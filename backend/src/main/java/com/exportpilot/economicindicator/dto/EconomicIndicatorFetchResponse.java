package com.exportpilot.economicindicator.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record EconomicIndicatorFetchResponse(

        Long countryId,
        String countryIso2Code,
        String countryName,

        Integer startYear,
        Integer endYear,

        Integer requestedIndicatorCount,
        Integer savedRecordCount,
        Integer availableRecordCount,
        Integer missingRecordCount,
        Integer staleRecordCount,

        OffsetDateTime retrievedAt,

        List<EconomicIndicatorResponse> indicators
) {
}