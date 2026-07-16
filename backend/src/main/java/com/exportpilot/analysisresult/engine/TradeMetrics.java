package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record TradeMetrics(
        Integer firstYear,
        Integer lastYear,
        Integer availableYearCount,

        BigDecimal firstYearTradeValueUsd,
        BigDecimal lastYearTradeValueUsd,
        BigDecimal totalTradeValueUsd,
        BigDecimal averageTradeValueUsd,

        BigDecimal absoluteGrowthUsd,
        BigDecimal growthRatePercent,
        BigDecimal cagrPercent
) {
}