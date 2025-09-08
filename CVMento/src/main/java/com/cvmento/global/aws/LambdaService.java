package com.cvmento.global.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaService {

    private final AWSLambda awsLambda;

    @Value("${cloud.aws.lambda.function-name}")
    private String lambdaFunctionName;

    public String invokeLambda(String payload) {
        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(lambdaFunctionName)
                .withPayload(payload);

        InvokeResult invokeResult = null;
        try {
            invokeResult = awsLambda.invoke(invokeRequest);

            if (invokeResult.getFunctionError() != null) {
                log.error("Lambda function error: {}", invokeResult.getFunctionError());
                throw new RuntimeException("Lambda invocation failed with function error.");
            }

            if (invokeResult.getStatusCode() != 200) {
                log.error("Lambda invocation failed with status code: {}", invokeResult.getStatusCode());
                throw new RuntimeException("Lambda invocation failed with status code: " + invokeResult.getStatusCode());
            }

            if (invokeResult.getPayload() != null) {
                return new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error("Error invoking Lambda function: {}", e.getMessage(), e);
            throw new RuntimeException("Error invoking Lambda function.", e);
        }
    }
}
