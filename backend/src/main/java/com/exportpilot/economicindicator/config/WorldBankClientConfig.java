package com.exportpilot.economicindicator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(WorldBankProperties.class)
public class WorldBankClientConfig {

    @Bean
    RestClient worldBankRestClient(
            WorldBankProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Duration.ofSeconds(
                        properties.connectTimeoutSeconds()
                )
        );

        requestFactory.setReadTimeout(
                Duration.ofSeconds(
                        properties.readTimeoutSeconds()
                )
        );

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(
                        "Accept",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}