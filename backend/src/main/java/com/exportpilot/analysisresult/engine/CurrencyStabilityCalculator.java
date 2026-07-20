package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class CurrencyStabilityCalculator {

    private static final BigDecimal AVERAGE_CHANGE_WEIGHT =
            new BigDecimal("0.45");

    private static final BigDecimal VOLATILITY_WEIGHT =
            new BigDecimal("0.40");

    private static final BigDecimal CUMULATIVE_CHANGE_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 10;

    private final MinMaxScoreNormalizer normalizer;

    public CurrencyStabilityCalculator(
            MinMaxScoreNormalizer normalizer
    ) {
        this.normalizer = normalizer;
    }

    public CurrencyStabilityMetrics calculate(
            CurrencyStabilityRawMetrics countryMetrics,
            List<CurrencyStabilityRawMetrics> allCountryMetrics
    ) {
        if (countryMetrics == null) {
            return emptyMetrics();
        }

        BigDecimal dataCompleteness =
                calculateDataCompleteness(countryMetrics);

        if (!countryMetrics.hasMinimumRequiredData()) {
            return new CurrencyStabilityMetrics(
                    null,
                    null,
                    null,
                    null,
                    countryMetrics.averageAbsoluteChangePercent(),
                    countryMetrics.volatilityPercent(),
                    countryMetrics.cumulativeChangePercent(),
                    dataCompleteness
            );
        }

        BigDecimal averageChangeScore =
                normalize(
                        countryMetrics
                                .averageAbsoluteChangePercent(),
                        allCountryMetrics,
                        CurrencyStabilityRawMetrics
                                ::averageAbsoluteChangePercent,
                        ScoreDirection.LOWER_IS_BETTER
                );

        BigDecimal volatilityScore =
                normalize(
                        countryMetrics.volatilityPercent(),
                        allCountryMetrics,
                        CurrencyStabilityRawMetrics
                                ::volatilityPercent,
                        ScoreDirection.LOWER_IS_BETTER
                );

        BigDecimal cumulativeChangeScore =
                normalizeAbsoluteValue(
                        countryMetrics.cumulativeChangePercent(),
                        allCountryMetrics,
                        CurrencyStabilityRawMetrics
                                ::cumulativeChangePercent
                );

        BigDecimal currencyStabilityScore =
                calculateWeightedScore(
                        averageChangeScore,
                        volatilityScore,
                        cumulativeChangeScore
                );

        return new CurrencyStabilityMetrics(
                averageChangeScore,
                volatilityScore,
                cumulativeChangeScore,
                currencyStabilityScore,
                countryMetrics.averageAbsoluteChangePercent(),
                countryMetrics.volatilityPercent(),
                countryMetrics.cumulativeChangePercent(),
                dataCompleteness
        );
    }

    private BigDecimal normalize(
            BigDecimal countryValue,
            List<CurrencyStabilityRawMetrics> allCountryMetrics,
            Function<CurrencyStabilityRawMetrics, BigDecimal> extractor,
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

    private BigDecimal normalizeAbsoluteValue(
            BigDecimal countryValue,
            List<CurrencyStabilityRawMetrics> allCountryMetrics,
            Function<CurrencyStabilityRawMetrics, BigDecimal> extractor
    ) {
        if (countryValue == null
                || allCountryMetrics == null
                || allCountryMetrics.isEmpty()) {
            return null;
        }

        BigDecimal countryAbsoluteValue =
                countryValue.abs();

        List<BigDecimal> availableAbsoluteValues =
                allCountryMetrics.stream()
                        .filter(Objects::nonNull)
                        .map(extractor)
                        .filter(Objects::nonNull)
                        .map(BigDecimal::abs)
                        .toList();

        if (availableAbsoluteValues.isEmpty()) {
            return null;
        }

        BigDecimal minimumValue =
                availableAbsoluteValues.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(null);

        BigDecimal maximumValue =
                availableAbsoluteValues.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(null);

        return normalizer.normalize(
                countryAbsoluteValue,
                minimumValue,
                maximumValue,
                ScoreDirection.LOWER_IS_BETTER
        );
    }

    private BigDecimal calculateWeightedScore(
            BigDecimal averageChangeScore,
            BigDecimal volatilityScore,
            BigDecimal cumulativeChangeScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (averageChangeScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            averageChangeScore.multiply(
                                    AVERAGE_CHANGE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            AVERAGE_CHANGE_WEIGHT
                    );
        }

        if (volatilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            volatilityScore.multiply(
                                    VOLATILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            VOLATILITY_WEIGHT
                    );
        }

        if (cumulativeChangeScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            cumulativeChangeScore.multiply(
                                    CUMULATIVE_CHANGE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            CUMULATIVE_CHANGE_WEIGHT
                    );
        }

        if (availableWeightSum.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            return null;
        }

        return weightedScoreSum
                .divide(
                        availableWeightSum,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateDataCompleteness(
            CurrencyStabilityRawMetrics metrics
    ) {
        if (metrics == null
                || metrics.expectedExchangeRateCount() == null
                || metrics.expectedExchangeRateCount() <= 0
                || metrics.availableExchangeRateCount() == null) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal.valueOf(
                        metrics.availableExchangeRateCount()
                )
                .divide(
                        BigDecimal.valueOf(
                                metrics.expectedExchangeRateCount()
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

    private CurrencyStabilityMetrics emptyMetrics() {
        return new CurrencyStabilityMetrics(
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