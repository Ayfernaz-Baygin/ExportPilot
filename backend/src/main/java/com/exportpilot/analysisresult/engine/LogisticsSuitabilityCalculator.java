package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class LogisticsSuitabilityCalculator {

    private static final BigDecimal DISTANCE_WEIGHT =
            new BigDecimal("0.35");

    private static final BigDecimal TRANSIT_DURATION_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal TRANSPORT_MODE_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal PORT_CONNECTIVITY_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 10;

    public LogisticsSuitabilityMetrics calculate(
            BigDecimal approximateDistanceKm,
            Integer averageTransitDays,
            String transportMode,
            BigDecimal portConnectivityIndex
    ) {
        BigDecimal distanceScore =
                calculateDistanceScore(approximateDistanceKm);

        BigDecimal transitDurationScore =
                calculateTransitDurationScore(averageTransitDays);

        BigDecimal transportModeScore =
                calculateTransportModeScore(transportMode);

        BigDecimal portConnectivityScore =
                calculatePortConnectivityScore(
                        portConnectivityIndex
                );

        BigDecimal logisticsSuitabilityScore =
                calculateWeightedScore(
                        distanceScore,
                        transitDurationScore,
                        transportModeScore,
                        portConnectivityScore
                );

        BigDecimal dataCompleteness =
                calculateDataCompleteness(
                        approximateDistanceKm,
                        averageTransitDays,
                        transportMode,
                        portConnectivityIndex
                );

        return new LogisticsSuitabilityMetrics(
                distanceScore,
                transitDurationScore,
                transportModeScore,
                portConnectivityScore,
                logisticsSuitabilityScore,
                approximateDistanceKm,
                averageTransitDays,
                transportMode,
                portConnectivityIndex,
                dataCompleteness
        );
    }

    private BigDecimal calculateDistanceScore(
            BigDecimal distanceKm
    ) {
        if (distanceKm == null) {
            return null;
        }

        if (distanceKm.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Distance cannot be negative."
            );
        }

        if (distanceKm.compareTo(
                new BigDecimal("1000")
        ) <= 0) {
            return new BigDecimal("100.00");
        }

        if (distanceKm.compareTo(
                new BigDecimal("2000")
        ) <= 0) {
            return new BigDecimal("85.00");
        }

        if (distanceKm.compareTo(
                new BigDecimal("4000")
        ) <= 0) {
            return new BigDecimal("70.00");
        }

        if (distanceKm.compareTo(
                new BigDecimal("7000")
        ) <= 0) {
            return new BigDecimal("50.00");
        }

        if (distanceKm.compareTo(
                new BigDecimal("10000")
        ) <= 0) {
            return new BigDecimal("30.00");
        }

        return new BigDecimal("15.00");
    }

    private BigDecimal calculateTransitDurationScore(
            Integer transitDays
    ) {
        if (transitDays == null) {
            return null;
        }

        if (transitDays < 0) {
            throw new IllegalArgumentException(
                    "Transit duration cannot be negative."
            );
        }

        if (transitDays <= 3) {
            return new BigDecimal("100.00");
        }

        if (transitDays <= 7) {
            return new BigDecimal("85.00");
        }

        if (transitDays <= 14) {
            return new BigDecimal("65.00");
        }

        if (transitDays <= 21) {
            return new BigDecimal("45.00");
        }

        if (transitDays <= 35) {
            return new BigDecimal("25.00");
        }

        return new BigDecimal("10.00");
    }

    private BigDecimal calculateTransportModeScore(
            String transportMode
    ) {
        if (transportMode == null
                || transportMode.isBlank()) {
            return null;
        }

        return switch (
                transportMode.trim().toUpperCase()
        ) {
            case "ROAD" ->
                    new BigDecimal("90.00");

            case "RAIL" ->
                    new BigDecimal("85.00");

            case "SEA" ->
                    new BigDecimal("75.00");

            case "AIR" ->
                    new BigDecimal("65.00");

            case "MULTIMODAL" ->
                    new BigDecimal("80.00");

            default ->
                    new BigDecimal("50.00");
        };
    }

    private BigDecimal calculatePortConnectivityScore(
            BigDecimal portConnectivityIndex
    ) {
        if (portConnectivityIndex == null) {
            return null;
        }

        return portConnectivityIndex
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateWeightedScore(
            BigDecimal distanceScore,
            BigDecimal transitDurationScore,
            BigDecimal transportModeScore,
            BigDecimal portConnectivityScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (distanceScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            distanceScore.multiply(
                                    DISTANCE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            DISTANCE_WEIGHT
                    );
        }

        if (transitDurationScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            transitDurationScore.multiply(
                                    TRANSIT_DURATION_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TRANSIT_DURATION_WEIGHT
                    );
        }

        if (transportModeScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            transportModeScore.multiply(
                                    TRANSPORT_MODE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TRANSPORT_MODE_WEIGHT
                    );
        }

        if (portConnectivityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            portConnectivityScore.multiply(
                                    PORT_CONNECTIVITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            PORT_CONNECTIVITY_WEIGHT
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
            BigDecimal approximateDistanceKm,
            Integer averageTransitDays,
            String transportMode,
            BigDecimal portConnectivityIndex
    ) {
        int availableFieldCount = 0;

        if (approximateDistanceKm != null) {
            availableFieldCount++;
        }

        if (averageTransitDays != null) {
            availableFieldCount++;
        }

        if (transportMode != null
                && !transportMode.isBlank()) {
            availableFieldCount++;
        }

        if (portConnectivityIndex != null) {
            availableFieldCount++;
        }

        return BigDecimal.valueOf(
                        availableFieldCount
                )
                .divide(
                        BigDecimal.valueOf(4),
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