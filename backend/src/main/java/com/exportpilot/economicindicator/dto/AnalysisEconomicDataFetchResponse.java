package com.exportpilot.economicindicator.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AnalysisEconomicDataFetchResponse(

        Long analysisId,

        Integer startYear,
        Integer endYear,

        Integer attemptedCountryCount,
        Integer successfulCountryCount,
        Integer failedCountryCount,

        Integer totalSavedRecordCount,
        Integer totalAvailableRecordCount,
        Integer totalMissingRecordCount,
        Integer totalStaleRecordCount,

        OffsetDateTime completedAt,

        List<CountryEconomicFetchSummary> countries
) {
}