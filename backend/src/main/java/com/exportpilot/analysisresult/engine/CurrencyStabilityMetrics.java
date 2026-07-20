package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record CurrencyStabilityMetrics(

        BigDecimal averageChangeScore,

        BigDecimal volatilityScore,

        BigDecimal cumulativeChangeScore,

        BigDecimal currencyStabilityScore,

        BigDecimal averageAbsoluteChangePercent,

        BigDecimal volatilityPercent,

        BigDecimal cumulativeChangePercent,

        BigDecimal dataCompleteness

) {
}