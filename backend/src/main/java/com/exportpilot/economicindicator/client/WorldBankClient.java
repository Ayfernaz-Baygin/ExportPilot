package com.exportpilot.economicindicator.client;


import com.exportpilot.economicindicator.config.WorldBankProperties;
import com.exportpilot.economicindicator.dto.WorldBankIndicatorResult;
import com.exportpilot.economicindicator.dto.WorldBankIndicatorValueResponse;
import com.exportpilot.economicindicator.dto.WorldBankResponseMetadata;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorldBankClient {

    private final RestClient restClient;
    private final WorldBankProperties properties;

    public WorldBankClient(
            RestClient worldBankRestClient,
            WorldBankProperties properties
    ) {
        this.restClient = worldBankRestClient;
        this.properties = properties;
    }

    public WorldBankIndicatorResult fetchIndicator(
            String countryIso2Code,
            EconomicIndicatorType indicatorType,
            Integer startYear,
            Integer endYear
    ) {
        validateRequest(
                countryIso2Code,
                indicatorType,
                startYear,
                endYear
        );

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(
                                            "/country/{countryCode}"
                                                    + "/indicator/{indicatorCode}"
                                    )
                                    .queryParam("format", "json")
                                    .queryParam(
                                            "date",
                                            startYear + ":" + endYear
                                    )
                                    .queryParam(
                                            "per_page",
                                            properties.perPage()
                                    )
                                    .build(
                                            countryIso2Code
                                                    .trim()
                                                    .toUpperCase(),
                                            indicatorType
                                                    .getWorldBankCode()
                                    )
                    )
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, responseValue) -> {
                                throw new WorldBankApiException(
                                        "World Bank API returned HTTP "
                                                + responseValue
                                                .getStatusCode()
                                                .value()
                                                + " for country "
                                                + countryIso2Code
                                                + " and indicator "
                                                + indicatorType
                                                        .getWorldBankCode()
                                );
                            }
                    )
                    .body(JsonNode.class);

            return parseResponse(
                    response,
                    countryIso2Code,
                    indicatorType
            );

        } catch (WorldBankApiException exception) {
            throw exception;

        } catch (RestClientException exception) {
            throw new WorldBankApiException(
                    "World Bank API request failed for country "
                            + countryIso2Code
                            + " and indicator "
                            + indicatorType.getWorldBankCode(),
                    exception
            );
        }
    }

    private WorldBankIndicatorResult parseResponse(
            JsonNode root,
            String requestedCountryIso2Code,
            EconomicIndicatorType requestedIndicatorType
    ) {
        if (
                root == null
                        || !root.isArray()
                        || root.size() < 2
        ) {
            throw new WorldBankApiException(
                    "World Bank API returned an unexpected response format."
            );
        }

        WorldBankResponseMetadata metadata =
                parseMetadata(root.get(0));

        JsonNode valuesNode = root.get(1);

        if (
                valuesNode == null
                        || valuesNode.isNull()
        ) {
            return new WorldBankIndicatorResult(
                    metadata,
                    List.of()
            );
        }

        if (!valuesNode.isArray()) {
            throw new WorldBankApiException(
                    "World Bank API indicator data is not an array."
            );
        }

        List<WorldBankIndicatorValueResponse> values =
                new ArrayList<>();

        for (JsonNode valueNode : valuesNode) {
            values.add(
                    parseIndicatorValue(
                            valueNode,
                            requestedCountryIso2Code,
                            requestedIndicatorType
                    )
            );
        }

        return new WorldBankIndicatorResult(
                metadata,
                List.copyOf(values)
        );
    }

    private WorldBankResponseMetadata parseMetadata(
            JsonNode metadataNode
    ) {
        if (
                metadataNode == null
                        || metadataNode.isNull()
        ) {
            return new WorldBankResponseMetadata(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new WorldBankResponseMetadata(
                integerValue(metadataNode, "page"),
                integerValue(metadataNode, "pages"),
                integerValue(metadataNode, "per_page"),
                integerValue(metadataNode, "total"),
                textValue(metadataNode, "lastupdated")
        );
    }

    private WorldBankIndicatorValueResponse parseIndicatorValue(
            JsonNode node,
            String requestedCountryIso2Code,
            EconomicIndicatorType requestedIndicatorType
    ) {
        JsonNode indicatorNode = node.get("indicator");
        JsonNode countryNode = node.get("country");

        String indicatorCode =
                nestedTextValue(indicatorNode, "id");

        String indicatorName =
                nestedTextValue(indicatorNode, "value");

        String countryCodeFromResponse =
                nestedTextValue(countryNode, "id");

        String countryName =
                nestedTextValue(countryNode, "value");

        String countryIso3Code =
                textValue(node, "countryiso3code");

        Integer year = parseYear(
                textValue(node, "date")
        );

        BigDecimal value =
                decimalValue(node, "value");

        return new WorldBankIndicatorValueResponse(
                indicatorCode == null
                        ? requestedIndicatorType.getWorldBankCode()
                        : indicatorCode,

                indicatorName == null
                        ? requestedIndicatorType.getDisplayName()
                        : indicatorName,

                countryCodeFromResponse == null
                        ? requestedCountryIso2Code
                                .trim()
                                .toUpperCase()
                        : countryCodeFromResponse,

                countryIso3Code,
                countryName,
                year,
                value,
                textValue(node, "unit"),
                textValue(node, "obs_status"),
                integerValue(node, "decimal")
        );
    }

    private String textValue(
            JsonNode node,
            String fieldName
    ) {
        if (
                node == null
                        || node.isNull()
        ) {
            return null;
        }

        JsonNode field = node.get(fieldName);

        if (
                field == null
                        || field.isNull()
        ) {
            return null;
        }

        String value = field.asText();

        return value == null || value.isBlank()
                ? null
                : value;
    }

    private String nestedTextValue(
            JsonNode node,
            String fieldName
    ) {
        return textValue(node, fieldName);
    }

    private Integer integerValue(
            JsonNode node,
            String fieldName
    ) {
        if (
                node == null
                        || node.isNull()
        ) {
            return null;
        }

        JsonNode field = node.get(fieldName);

        if (
                field == null
                        || field.isNull()
        ) {
            return null;
        }

        if (field.isInt() || field.isLong()) {
            return field.asInt();
        }

        try {
            return Integer.valueOf(field.asText());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal decimalValue(
            JsonNode node,
            String fieldName
    ) {
        if (
                node == null
                        || node.isNull()
        ) {
            return null;
        }

        JsonNode field = node.get(fieldName);

        if (
                field == null
                        || field.isNull()
        ) {
            return null;
        }

        try {
            return field.decimalValue();
        } catch (ArithmeticException exception) {
            throw new WorldBankApiException(
                    "World Bank API returned an invalid decimal value.",
                    exception
            );
        }
    }

    private Integer parseYear(String yearText) {
        if (
                yearText == null
                        || yearText.isBlank()
        ) {
            return null;
        }

        try {
            return Integer.valueOf(yearText);
        } catch (NumberFormatException exception) {
            throw new WorldBankApiException(
                    "World Bank API returned an invalid year: "
                            + yearText,
                    exception
            );
        }
    }

    private void validateRequest(
            String countryIso2Code,
            EconomicIndicatorType indicatorType,
            Integer startYear,
            Integer endYear
    ) {
        if (
                countryIso2Code == null
                        || countryIso2Code.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Country ISO2 code is required."
            );
        }

        if (countryIso2Code.trim().length() != 2) {
            throw new IllegalArgumentException(
                    "Country ISO2 code must contain exactly 2 characters."
            );
        }

        if (indicatorType == null) {
            throw new IllegalArgumentException(
                    "Economic indicator type is required."
            );
        }

        if (startYear == null || endYear == null) {
            throw new IllegalArgumentException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new IllegalArgumentException(
                    "Start year cannot be greater than end year."
            );
        }
    }
}