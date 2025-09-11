package com.cvmento.global.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.cvmento.global.exception.customException.LambdaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaService {

    private final AWSLambda lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.lambda.function-name}")
    private String functionName;

    public String invokeLambdaOcr(MultipartFile file) {
        MDC.put("spanId", "lambda-ocr-service");

        try {
            // 파일을 Base64로 변환
            MDC.put("spanId", "file-encoding");
            String base64File = Base64.getEncoder().encodeToString(file.getBytes());

            // null 체크 후 기본값 설정
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            MDC.put("spanId", "lambda-ocr-service");
            log.info("Lambda OCR 처리 시작 - 함수: {}, 파일크기: {}bytes, 타입: {}",
                    functionName, file.getSize(), contentType);

            // Lambda 페이로드 구성
            Map<String, Object> payload = Map.of(
                    "fileContent", base64File,
                    "fileName", fileName,
                    "contentType", contentType
            );

            MDC.put("spanId", "payload-serialization");
            String payloadJson = objectMapper.writeValueAsString(payload);

            MDC.put("spanId", "aws-lambda-api");
            // Lambda 호출
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(functionName)
                    .withPayload(payloadJson)
                    .withInvocationType(InvocationType.RequestResponse);

            InvokeResult result = lambdaClient.invoke(invokeRequest);

            MDC.put("spanId", "lambda-response-parsing");
            // 응답 처리
            String ocrResult = parseLambdaResponse(result);

            MDC.put("spanId", "lambda-ocr-service");
            log.info("Lambda OCR 처리 완료 - 결과텍스트길이: {}chars", ocrResult.length());

            return ocrResult;

        } catch (IOException e) {
            log.error("파일 처리 중 오류: {}", e.getMessage(), e);
            throw new LambdaException("파일을 읽는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("Lambda OCR 호출 중 오류: {}", e.getMessage(), e);
            throw new LambdaException("OCR 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String parseLambdaResponse(InvokeResult result) throws IOException {
        // Lambda 실행 오류 체크
        if (result.getFunctionError() != null) {
            String errorMessage = new String(result.getPayload().array());
            log.error("Lambda 실행 오류: {}", result.getFunctionError());
            throw new LambdaException("OCR 처리 실패: " + result.getFunctionError());
        }

        // HTTP 상태 코드 체크
        if (result.getStatusCode() != 200) {
            log.error("Lambda 응답 상태코드: {}", result.getStatusCode());
            throw new LambdaException("Lambda 호출 실패");
        }

        // 응답 파싱
        String responseJson = new String(result.getPayload().array());
        log.info("Lambda 응답 수신 완료 - 응답크기: {}chars", responseJson.length());

        try {
            JsonNode jsonNode = objectMapper.readTree(responseJson);

            // statusCode 확인
            if (jsonNode.has("statusCode")) {
                int statusCode = jsonNode.get("statusCode").asInt();
                if (statusCode != 200) {
                    String errorBody = jsonNode.has("body") ? jsonNode.get("body").asText() : "Unknown error";
                    throw new LambdaException("Lambda 처리 실패: " + errorBody);
                }
            }

            // body에서 텍스트 추출
            if (jsonNode.has("body")) {
                String bodyText = jsonNode.get("body").asText();
                log.debug("Lambda body 추출 성공 - 길이: {}chars", bodyText.length());
                return bodyText;
            }

            // 응답 전체가 텍스트인 경우
            log.debug("Lambda 전체 응답을 텍스트로 처리");
            return responseJson;

        } catch (JsonProcessingException e) {
            // JSON이 아닌 경우 그대로 반환
            log.debug("Lambda 응답이 JSON이 아님 - 원본 반환");
            return responseJson;
        }
    }
}
