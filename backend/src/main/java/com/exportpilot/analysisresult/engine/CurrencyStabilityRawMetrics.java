package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record CurrencyStabilityRawMetrics(

        Long countryId,

        BigDecimal averageAbsoluteChangePercent,

        BigDecimal volatilityPercent,

        BigDecimal cumulativeChangePercent,

        Integer availableExchangeRateCount,

        Integer expectedExchangeRateCount

) {

    public boolean hasMinimumRequiredData() {
        return availableExchangeRateCount != null
                && availableExchangeRateCount >= 3
                && averageAbsoluteChangePercent != null
                && volatilityPercent != null;
    }
}