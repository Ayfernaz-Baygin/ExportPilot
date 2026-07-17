package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class MacroeconomicStabilityCalculator {

    private static final BigDecimal GDP_GROWTH_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal INFLATION_WEIGHT =
            new BigDecimal("0.25");

    private static final BigDecimal UNEMPLOYMENT_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal GDP_PER_CAPITA_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal TRADE_OPENNESS_WEIGHT =
            new BigDecimal("0.10");

    private static final BigDecimal ECONOMIC_VOLATILITY_WEIGHT =
            new BigDecimal("0.05");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 10;

    private final MinMaxScoreNormalizer normalizer;

    public MacroeconomicStabilityCalculator(
            MinMaxScoreNormalizer normalizer
    ) {
        this.normalizer = normalizer;
    }

    public MacroeconomicMetrics calculate(
            MacroeconomicRawMetrics countryMetrics,
            List<MacroeconomicRawMetrics> allCountryMetrics
    ) {
        if (countryMetrics == null) {
            return emptyMetrics();
        }

        if (!countryMetrics.hasMinimumRequiredData()) {
            return new MacroeconomicMetrics(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    calculateDataCompleteness(countryMetrics)
            );
        }

        BigDecimal gdpGrowthScore =
                normalize(
                        countryMetrics.gdpGrowthPercent(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::gdpGrowthPercent,
                        ScoreDirection.HIGHER_IS_BETTER
                );

        BigDecimal inflationScore =
                normalize(
                        countryMetrics.inflationPercent(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::inflationPercent,
                        ScoreDirection.LOWER_IS_BETTER
                );

        BigDecimal unemploymentScore =
                normalize(
                        countryMetrics.unemploymentPercent(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::unemploymentPercent,
                        ScoreDirection.LOWER_IS_BETTER
                );

        BigDecimal gdpPerCapitaScore =
                normalize(
                        countryMetrics.gdpPerCapitaUsd(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::gdpPerCapitaUsd,
                        ScoreDirection.HIGHER_IS_BETTER
                );

        BigDecimal tradeOpennessScore =
                normalize(
                        countryMetrics.tradeGdpRatioPercent(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::tradeGdpRatioPercent,
                        ScoreDirection.HIGHER_IS_BETTER
                );

        BigDecimal economicVolatilityScore =
                normalize(
                        countryMetrics.gdpGrowthVolatility(),
                        allCountryMetrics,
                        MacroeconomicRawMetrics::gdpGrowthVolatility,
                        ScoreDirection.LOWER_IS_BETTER
                );

        BigDecimal weightedScore =
                calculateWeightedScore(
                        gdpGrowthScore,
                        inflationScore,
                        unemploymentScore,
                        gdpPerCapitaScore,
                        tradeOpennessScore,
                        economicVolatilityScore
                );

        return new MacroeconomicMetrics(
                gdpGrowthScore,
                inflationScore,
                unemploymentScore,
                gdpPerCapitaScore,
                tradeOpennessScore,
                economicVolatilityScore,
                weightedScore,
                calculateDataCompleteness(countryMetrics)
        );
    }

    private BigDecimal normalize(
            BigDecimal countryValue,
            List<MacroeconomicRawMetrics> allCountryMetrics,
            Function<MacroeconomicRawMetrics, BigDecimal> extractor,
            ScoreDirection direction
    ) {
        if (countryValue == null
                || allCountryMetrics == null
                || allCountryMetrics.isEmpty()) {
            return null;
        }

        List<BigDecimal> availableValues =
                allCountryMetrics.stream()
                        .filter(Objects::nonNull)
                        .map(extractor)
                        .filter(Objects::nonNull)
                        .toList();

        if (availableValues.isEmpty()) {
            return null;
        }

        BigDecimal minimumValue =
                availableValues.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(null);

        BigDecimal maximumValue =
                availableValues.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(null);

        return normalizer.normalize(
                countryValue,
                minimumValue,
                maximumValue,
                direction
        );
    }

    private BigDecimal calculateWeightedScore(
            BigDecimal gdpGrowthScore,
            BigDecimal inflationScore,
            BigDecimal unemploymentScore,
            BigDecimal gdpPerCapitaScore,
            BigDecimal tradeOpennessScore,
            BigDecimal economicVolatilityScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (gdpGrowthScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    gdpGrowthScore.multiply(
                            GDP_GROWTH_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    GDP_GROWTH_WEIGHT
            );
        }

        if (inflationScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    inflationScore.multiply(
                            INFLATION_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    INFLATION_WEIGHT
            );
        }

        if (unemploymentScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    unemploymentScore.multiply(
                            UNEMPLOYMENT_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    UNEMPLOYMENT_WEIGHT
            );
        }

        if (gdpPerCapitaScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    gdpPerCapitaScore.multiply(
                            GDP_PER_CAPITA_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    GDP_PER_CAPITA_WEIGHT
            );
        }

        if (tradeOpennessScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    tradeOpennessScore.multiply(
                            TRADE_OPENNESS_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    TRADE_OPENNESS_WEIGHT
            );
        }

        if (economicVolatilityScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    economicVolatilityScore.multiply(
                            ECONOMIC_VOLATILITY_WEIGHT
                    )
            );

            availableWeightSum = availableWeightSum.add(
                    ECONOMIC_VOLATILITY_WEIGHT
            );
        }

        if (availableWeightSum.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return weightedScoreSum
                .divide(
                        availableWeightSum,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .min(MAX_SCORE)
                .max(BigDecimal.ZERO)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateDataCompleteness(
            MacroeconomicRawMetrics metrics
    ) {
        if (metrics == null
                || metrics.expectedComponentCount() == null
                || metrics.expectedComponentCount() <= 0
                || metrics.availableComponentCount() == null) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal.valueOf(
                        metrics.availableComponentCount()
                )
                .divide(
                        BigDecimal.valueOf(
                                metrics.expectedComponentCount()
                        ),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(MAX_SCORE)
                .min(MAX_SCORE)
                .max(BigDecimal.ZERO)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private MacroeconomicMetrics emptyMetrics() {
        return new MacroeconomicMetrics(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.ZERO.setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                )
        );
    }
}