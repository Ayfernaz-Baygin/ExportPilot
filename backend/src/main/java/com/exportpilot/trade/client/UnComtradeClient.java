package com.exportpilot.trade.client;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UnComtradeClient {

    private static final String FINAL_DATA_PATH =
            "/data/v1/get/C/A/HS";

    private static final int MAX_RETRY_COUNT =
            3;

    private static final long INITIAL_RETRY_DELAY_MS =
            2_000L;

    private final RestClient restClient;
    private final String apiKey;

    public UnComtradeClient(
            @Value("${integrations.un-comtrade.base-url}")
            String baseUrl,
            @Value("${UN_COMTRADE_API_KEY:}")
            String apiKey
    ) {
        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();

        this.apiKey = apiKey;
    }

    /**
     * Tek yıllık sorgular için geriye dönük uyumlu metot.
     */
    public UnComtradeApiResponse fetchAnnualTradeData(
            String commodityCode,
            Integer year,
            Integer reporterCode,
            Integer partnerCode,
            String flowCode,
            Integer maxRecords
    ) {
        if (year == null) {
            throw new BusinessRuleException(
                    "UN Comtrade year is required."
            );
        }

        return fetchAnnualTradeData(
                commodityCode,
                List.of(year),
                reporterCode,
                partnerCode,
                flowCode,
                maxRecords
        );
    }

    /**
     * Birden fazla yılı tek UN Comtrade isteğinde sorgular.
     *
     * Örnek period:
     * 2021,2022,2023,2024,2025
     */
    public UnComtradeApiResponse fetchAnnualTradeData(
            String commodityCode,
            List<Integer> years,
            Integer reporterCode,
            Integer partnerCode,
            String flowCode,
            Integer maxRecords
    ) {
        validateConfiguration();

        String period =
                buildPeriodParameter(years);

        validateRequest(
                commodityCode,
                years,
                reporterCode,
                partnerCode,
                flowCode,
                maxRecords
        );

        return executeWithRetry(
                commodityCode,
                period,
                reporterCode,
                partnerCode,
                flowCode,
                maxRecords
        );
    }

    private UnComtradeApiResponse executeWithRetry(
            String commodityCode,
            String period,
            Integer reporterCode,
            Integer partnerCode,
            String flowCode,
            Integer maxRecords
    ) {
        int attempt = 0;
        long retryDelay =
                INITIAL_RETRY_DELAY_MS;

        while (true) {
            try {
                return executeRequest(
                        commodityCode,
                        period,
                        reporterCode,
                        partnerCode,
                        flowCode,
                        maxRecords
                );
            } catch (RateLimitException exception) {
                attempt++;

                if (attempt >= MAX_RETRY_COUNT) {
                    throw new BusinessRuleException(
                            "UN Comtrade request limit was exceeded "
                                    + "after "
                                    + MAX_RETRY_COUNT
                                    + " attempts. Please try again later."
                    );
                }

                waitBeforeRetry(retryDelay);

                retryDelay *= 2;
            }
        }
    }

    private UnComtradeApiResponse executeRequest(
            String commodityCode,
            String period,
            Integer reporterCode,
            Integer partnerCode,
            String flowCode,
            Integer maxRecords
    ) {
        try {
            UnComtradeApiResponse apiResponse =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(FINAL_DATA_PATH)
                                            .queryParam(
                                                    "cmdCode",
                                                    commodityCode
                                            )
                                            .queryParam(
                                                    "period",
                                                    period
                                            )
                                            .queryParam(
                                                    "reporterCode",
                                                    reporterCode
                                            )
                                            .queryParam(
                                                    "partnerCode",
                                                    partnerCode
                                            )
                                            .queryParam(
                                                    "flowCode",
                                                    flowCode
                                            )
                                            .queryParam(
                                                    "maxrecords",
                                                    maxRecords
                                            )
                                            .queryParam(
                                                    "subscription-key",
                                                    apiKey
                                            )
                                            .build()
                            )
                            .retrieve()
                            .onStatus(
                                    status ->
                                            status.value() == 401
                                                    || status.value() == 403,
                                    (request, response) -> {
                                        throw new BusinessRuleException(
                                                "UN Comtrade rejected the API key."
                                        );
                                    }
                            )
                            .onStatus(
                                    status ->
                                            status.value() == 429,
                                    (request, response) -> {
                                        throw new RateLimitException();
                                    }
                            )
                            .onStatus(
                                    HttpStatusCode::is4xxClientError,
                                    (request, response) -> {
                                        throw new BusinessRuleException(
                                                "UN Comtrade rejected the request. "
                                                        + "HTTP status: "
                                                        + response
                                                        .getStatusCode()
                                                        .value()
                                        );
                                    }
                            )
                            .onStatus(
                                    HttpStatusCode::is5xxServerError,
                                    (request, response) -> {
                                        throw new BusinessRuleException(
                                                "UN Comtrade service is currently "
                                                        + "unavailable. HTTP status: "
                                                        + response
                                                        .getStatusCode()
                                                        .value()
                                        );
                                    }
                            )
                            .body(
                                    UnComtradeApiResponse.class
                            );

            if (apiResponse == null) {
                throw new BusinessRuleException(
                        "UN Comtrade returned an empty response."
                );
            }

            return apiResponse;
        } catch (RateLimitException exception) {
            throw exception;
        } catch (BusinessRuleException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessRuleException(
                    "UN Comtrade request failed: "
                            + exception.getMessage()
            );
        }
    }

    private String buildPeriodParameter(
            List<Integer> years
    ) {
        if (years == null || years.isEmpty()) {
            throw new BusinessRuleException(
                    "At least one UN Comtrade year is required."
            );
        }

        return years.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(
                        Collectors.joining(",")
                );
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessRuleException(
                    "UN Comtrade API key is not configured. "
                            + "Set the UN_COMTRADE_API_KEY "
                            + "environment variable."
            );
        }
    }

    private void validateRequest(
            String commodityCode,
            List<Integer> years,
            Integer reporterCode,
            Integer partnerCode,
            String flowCode,
            Integer maxRecords
    ) {
        if (!StringUtils.hasText(
                commodityCode
        )) {
            throw new BusinessRuleException(
                    "UN Comtrade commodity code is required."
            );
        }

        if (years == null || years.isEmpty()) {
            throw new BusinessRuleException(
                    "At least one UN Comtrade year is required."
            );
        }

        boolean containsInvalidYear =
                years.stream()
                        .anyMatch(year ->
                                year == null
                                        || year < 1900
                                        || year > 2100
                        );

        if (containsInvalidYear) {
            throw new BusinessRuleException(
                    "UN Comtrade years must be between "
                            + "1900 and 2100."
            );
        }

        if (reporterCode == null
                || reporterCode <= 0) {
            throw new BusinessRuleException(
                    "A valid reporter M49 code is required."
            );
        }

        if (partnerCode == null
                || partnerCode < 0) {
            throw new BusinessRuleException(
                    "A valid partner M49 code is required."
            );
        }

        if (!"M".equalsIgnoreCase(flowCode)
                && !"X".equalsIgnoreCase(flowCode)) {
            throw new BusinessRuleException(
                    "UN Comtrade flow code must be M or X."
            );
        }

        if (maxRecords == null
                || maxRecords < 1
                || maxRecords > 100_000) {
            throw new BusinessRuleException(
                    "UN Comtrade maxRecords must be between "
                            + "1 and 100000."
            );
        }
    }

    private void waitBeforeRetry(
            long delayMilliseconds
    ) {
        try {
            Thread.sleep(
                    delayMilliseconds
            );
        } catch (InterruptedException exception) {
            Thread.currentThread()
                    .interrupt();

            throw new BusinessRuleException(
                    "UN Comtrade retry was interrupted."
            );
        }
    }

    private static class RateLimitException
            extends RuntimeException {
    }
}