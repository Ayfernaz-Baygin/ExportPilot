package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record MacroeconomicRawMetrics(

        Long countryId,

        BigDecimal gdpGrowthPercent,

        BigDecimal inflationPercent,

        BigDecimal unemploymentPercent,

        BigDecimal gdpPerCapitaUsd,

        BigDecimal tradeGdpRatioPercent,

        BigDecimal gdpGrowthVolatility,

        Integer availableComponentCount,

        Integer expectedComponentCount

) {

    public boolean hasMinimumRequiredData() {
        return availableComponentCount != null
                && availableComponentCount >= 4;
    }
}