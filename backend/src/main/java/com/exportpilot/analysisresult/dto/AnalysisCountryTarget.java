package com.exportpilot.analysisresult.dto;

public record AnalysisCountryTarget(
        Long countryId,
        String iso2Code,
        String countryName
) {
}