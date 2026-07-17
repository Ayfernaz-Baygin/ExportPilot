package com.exportpilot.analysisresult.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AnalysisCountryResultResponse(

        Long id,

        Long analysisId,

        Long countryId,
        String countryIso2Code,
        String countryIso3Code,
        String countryName,
        String countryRegion,

        Integer rankPosition,
        BigDecimal overallScore,

        BigDecimal importMarketSizeScore,
        BigDecimal importGrowthScore,
        BigDecimal turkeyExportPerformanceScore,
        BigDecimal marketShareOpportunityScore,
        BigDecimal competitiveAccessibilityScore,
        BigDecimal macroeconomicStabilityScore,
        BigDecimal currencyStabilityScore,
        BigDecimal logisticsSuitabilityScore,
        BigDecimal tariffSuitabilityScore,

        Integer firstYear,
        Integer lastYear,
        BigDecimal firstYearTradeValueUsd,
        BigDecimal lastYearTradeValueUsd,
        BigDecimal totalTradeValueUsd,
        BigDecimal averageTradeValueUsd,
        BigDecimal absoluteGrowthUsd,
        BigDecimal growthRatePercent,
        BigDecimal cagrPercent,

        Integer availableYearCount,
        BigDecimal dataCompleteness,

        OffsetDateTime calculatedAt,
        OffsetDateTime createdAt
) {
}