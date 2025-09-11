package com.cvmento.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }
}