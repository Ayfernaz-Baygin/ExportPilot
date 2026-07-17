package com.exportpilot.economicindicator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "world-bank.api")
public record WorldBankProperties(
        String baseUrl,
        String sourceName,
        Integer perPage,
        String transformationVersion,
        Integer connectTimeoutSeconds,
        Integer readTimeoutSeconds
) {

    public WorldBankProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.worldbank.org/v2";
        }

        if (sourceName == null || sourceName.isBlank()) {
            sourceName = "World Bank";
        }

        if (perPage == null || perPage <= 0) {
            perPage = 100;
        }

        if (
                transformationVersion == null
                        || transformationVersion.isBlank()
        ) {
            transformationVersion = "v1";
        }

        if (
                connectTimeoutSeconds == null
                        || connectTimeoutSeconds <= 0
        ) {
            connectTimeoutSeconds = 10;
        }

        if (
                readTimeoutSeconds == null
                        || readTimeoutSeconds <= 0
        ) {
            readTimeoutSeconds = 30;
        }
    }
}