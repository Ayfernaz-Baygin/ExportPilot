package com.exportpilot.economicindicator.dto;

public record CountryEconomicFetchSummary(

        Long countryId,
        String countryIso2Code,
        String countryName,

        boolean successful,

        Integer savedRecordCount,
        Integer availableRecordCount,
        Integer missingRecordCount,
        Integer staleRecordCount,

        String errorMessage
) {
}