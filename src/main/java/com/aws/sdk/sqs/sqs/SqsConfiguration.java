package com.aws.sdk.sqs.sqs;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfiguration {

    @Bean
    public SqsClient createSqsClient(InjectionPoint injectionPoint) {

        return SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
    }
}
