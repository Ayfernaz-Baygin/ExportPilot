package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TariffSuitabilityCalculator {

    private static final BigDecimal TARIFF_RATE_WEIGHT =
            new BigDecimal("0.50");

    private static final BigDecimal PREFERENTIAL_ACCESS_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal NON_TARIFF_BARRIER_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 10;

    public TariffSuitabilityMetrics calculate(
            BigDecimal appliedTariffRatePercent,
            Boolean preferentialTradeAgreement,
            BigDecimal nonTariffBarrierIndex
    ) {
        BigDecimal tariffRateScore =
                calculateTariffRateScore(
                        appliedTariffRatePercent
                );

        BigDecimal preferentialAccessScore =
                calculatePreferentialAccessScore(
                        preferentialTradeAgreement
                );

        BigDecimal nonTariffBarrierScore =
                calculateNonTariffBarrierScore(
                        nonTariffBarrierIndex
                );

        BigDecimal tariffSuitabilityScore =
                calculateWeightedScore(
                        tariffRateScore,
                        preferentialAccessScore,
                        nonTariffBarrierScore
                );

        BigDecimal dataCompleteness =
                calculateDataCompleteness(
                        appliedTariffRatePercent,
                        preferentialTradeAgreement,
                        nonTariffBarrierIndex
                );

        return new TariffSuitabilityMetrics(
                tariffRateScore,
                preferentialAccessScore,
                nonTariffBarrierScore,
                tariffSuitabilityScore,
                appliedTariffRatePercent,
                preferentialTradeAgreement,
                nonTariffBarrierIndex,
                dataCompleteness
        );
    }

    private BigDecimal calculateTariffRateScore(
            BigDecimal tariffRatePercent
    ) {
        if (tariffRatePercent == null) {
            return null;
        }

        if (tariffRatePercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Tariff rate cannot be negative."
            );
        }

        if (tariffRatePercent.compareTo(
                new BigDecimal("0")
        ) == 0) {
            return new BigDecimal("100.00");
        }

        if (tariffRatePercent.compareTo(
                new BigDecimal("2")
        ) <= 0) {
            return new BigDecimal("90.00");
        }

        if (tariffRatePercent.compareTo(
                new BigDecimal("5")
        ) <= 0) {
            return new BigDecimal("75.00");
        }

        if (tariffRatePercent.compareTo(
                new BigDecimal("10")
        ) <= 0) {
            return new BigDecimal("55.00");
        }

        if (tariffRatePercent.compareTo(
                new BigDecimal("20")
        ) <= 0) {
            return new BigDecimal("30.00");
        }

        return new BigDecimal("10.00");
    }

    private BigDecimal calculatePreferentialAccessScore(
            Boolean preferentialTradeAgreement
    ) {
        if (preferentialTradeAgreement == null) {
            return null;
        }

        return preferentialTradeAgreement
                ? new BigDecimal("100.00")
                : new BigDecimal("40.00");
    }

    private BigDecimal calculateNonTariffBarrierScore(
            BigDecimal nonTariffBarrierIndex
    ) {
        if (nonTariffBarrierIndex == null) {
            return null;
        }

        BigDecimal normalizedIndex =
                nonTariffBarrierIndex
                        .max(BigDecimal.ZERO)
                        .min(MAX_SCORE);

        return MAX_SCORE
                .subtract(normalizedIndex)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateWeightedScore(
            BigDecimal tariffRateScore,
            BigDecimal preferentialAccessScore,
            BigDecimal nonTariffBarrierScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (tariffRateScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            tariffRateScore.multiply(
                                    TARIFF_RATE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TARIFF_RATE_WEIGHT
                    );
        }

        if (preferentialAccessScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            preferentialAccessScore.multiply(
                                    PREFERENTIAL_ACCESS_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            PREFERENTIAL_ACCESS_WEIGHT
                    );
        }

        if (nonTariffBarrierScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            nonTariffBarrierScore.multiply(
                                    NON_TARIFF_BARRIER_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            NON_TARIFF_BARRIER_WEIGHT
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
            BigDecimal appliedTariffRatePercent,
            Boolean preferentialTradeAgreement,
            BigDecimal nonTariffBarrierIndex
    ) {
        int availableFieldCount = 0;

        if (appliedTariffRatePercent != null) {
            availableFieldCount++;
        }

        if (preferentialTradeAgreement != null) {
            availableFieldCount++;
        }

        if (nonTariffBarrierIndex != null) {
            availableFieldCount++;
        }

        return BigDecimal.valueOf(
                        availableFieldCount
                )
                .divide(
                        BigDecimal.valueOf(3),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}