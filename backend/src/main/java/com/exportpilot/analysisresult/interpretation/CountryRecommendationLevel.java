package com.exportpilot.analysisresult.interpretation;

import java.math.BigDecimal;

public enum CountryRecommendationLevel {

    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW;

    private static final BigDecimal VERY_HIGH_THRESHOLD =
            new BigDecimal("75");

    private static final BigDecimal HIGH_THRESHOLD =
            new BigDecimal("60");

    private static final BigDecimal MEDIUM_THRESHOLD =
            new BigDecimal("45");

    private static final BigDecimal LOW_THRESHOLD =
            new BigDecimal("30");

    public static CountryRecommendationLevel fromScore(
            BigDecimal overallScore
    ) {
        if (overallScore == null) {
            return VERY_LOW;
        }

        if (overallScore.compareTo(VERY_HIGH_THRESHOLD) >= 0) {
            return VERY_HIGH;
        }

        if (overallScore.compareTo(HIGH_THRESHOLD) >= 0) {
            return HIGH;
        }

        if (overallScore.compareTo(MEDIUM_THRESHOLD) >= 0) {
            return MEDIUM;
        }

        if (overallScore.compareTo(LOW_THRESHOLD) >= 0) {
            return LOW;
        }

        return VERY_LOW;
    }
}