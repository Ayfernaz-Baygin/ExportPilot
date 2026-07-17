package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record MacroeconomicMetrics(

        BigDecimal gdpGrowthScore,

        BigDecimal inflationScore,

        BigDecimal unemploymentScore,

        BigDecimal gdpPerCapitaScore,

        BigDecimal tradeOpennessScore,

        BigDecimal economicVolatilityScore,

        BigDecimal macroeconomicStabilityScore,

        BigDecimal dataCompleteness

) {
}