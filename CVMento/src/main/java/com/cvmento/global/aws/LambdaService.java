package com.cvmento.global.aws;

import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.core.SdkBytes;
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

/**
 * AWS Lambda 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.lambda.function-name}")
    private String functionName;

    /**
     * Lambda를 통한 OCR 처리.
     */
    public String invokeLambdaOcr(MultipartFile file) {
        MDC.put("spanId", "lambda-ocr-service");

        try {
            MDC.put("spanId", "file-encoding");
            String base64File = Base64.getEncoder().encodeToString(file.getBytes());

            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            MDC.put("spanId", "lambda-ocr-service");
            log.info("Lambda OCR 처리 시작 - 함수: {}, 파일크기: {}bytes, 타입: {}",
                    functionName, file.getSize(), contentType);

            Map<String, Object> payload = Map.of(
                    "fileContent", base64File,
                    "fileName", fileName,
                    "contentType", contentType
            );

            MDC.put("spanId", "payload-serialization");
            String payloadJson = objectMapper.writeValueAsString(payload);

            MDC.put("spanId", "aws-lambda-api");
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(payloadJson))
                    .invocationType(InvocationType.REQUEST_RESPONSE)
                    .build();

            InvokeResponse result = lambdaClient.invoke(invokeRequest);

            MDC.put("spanId", "lambda-response-parsing");
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

    /**
     * Lambda 응답 파싱 및 처리.
     */
    private String parseLambdaResponse(InvokeResponse result) throws IOException {
        if (result.functionError() != null) {
            String errorMessage = result.payload().asUtf8String();
            log.error("Lambda 실행 오류: {}", result.functionError());
            throw new LambdaException("OCR 처리 실패: " + result.functionError());
        }

        if (result.statusCode() != 200) {
            log.error("Lambda 응답 상태코드: {}", result.statusCode());
            throw new LambdaException("Lambda 호출 실패");
        }

        String responseJson = result.payload().asUtf8String();
        log.info("Lambda 응답 수신 완료 - 응답크기: {}chars", responseJson.length());

        try {
            JsonNode jsonNode = objectMapper.readTree(responseJson);

            if (jsonNode.has("statusCode")) {
                int statusCode = jsonNode.get("statusCode").asInt();
                if (statusCode != 200) {
                    String errorBody = jsonNode.has("body") ? jsonNode.get("body").asText() : "Unknown error";
                    throw new LambdaException("Lambda 처리 실패: " + errorBody);
                }
            }

            if (jsonNode.has("body")) {
                String bodyText = jsonNode.get("body").asText();
                log.debug("Lambda body 추출 성공 - 길이: {}chars", bodyText.length());
                return bodyText;
            }

            log.debug("Lambda 전체 응답을 텍스트로 처리");
            return responseJson;

        } catch (JsonProcessingException e) {
            log.debug("Lambda 응답이 JSON이 아님 - 원본 반환");
            return responseJson;
        }
    }
}