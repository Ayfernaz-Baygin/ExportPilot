package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record LogisticsSuitabilityMetrics(

        BigDecimal distanceScore,

        BigDecimal transitDurationScore,

        BigDecimal transportModeScore,

        BigDecimal portConnectivityScore,

        BigDecimal logisticsSuitabilityScore,

        BigDecimal approximateDistanceKm,

        Integer averageTransitDays,

        String transportMode,

        BigDecimal portConnectivityIndex,

        BigDecimal dataCompleteness

) {
}
