package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record CompetitiveAccessibilityMetrics(

        BigDecimal competitiveAccessibilityScore,

        Integer supplierCount,

        BigDecimal supplierConcentrationHhi,

        Integer turkeySupplierRank,

        BigDecimal turkeyMarketSharePercent,

        BigDecimal leaderMarketSharePercent,

        BigDecimal distanceToLeaderPercent,

        BigDecimal turkeyUnitValueUsdPerKg,

        BigDecimal marketAverageUnitValueUsdPerKg

) {
}