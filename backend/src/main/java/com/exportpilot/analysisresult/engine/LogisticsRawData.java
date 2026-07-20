package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record LogisticsRawData(

        BigDecimal approximateDistanceKm,

        Integer averageTransitDays,

        String transportMode,

        BigDecimal portConnectivityIndex,

        String originLocation,

        String destinationLocation,

        String dataSource,

        boolean estimated

) {
}
