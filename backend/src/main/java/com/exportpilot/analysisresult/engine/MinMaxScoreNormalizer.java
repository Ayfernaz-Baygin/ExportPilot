package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MinMaxScoreNormalizer {

    private static final BigDecimal MIN_SCORE =
            BigDecimal.ZERO;

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final BigDecimal NEUTRAL_SCORE =
            new BigDecimal("50.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 10;

    public BigDecimal normalize(
            BigDecimal value,
            BigDecimal minimumValue,
            BigDecimal maximumValue,
            ScoreDirection direction
    ) {
        if (value == null
                || minimumValue == null
                || maximumValue == null
                || direction == null) {
            return null;
        }

        if (maximumValue.compareTo(minimumValue) < 0) {
            throw new IllegalArgumentException(
                    "Maximum value cannot be smaller than minimum value."
            );
        }

        if (maximumValue.compareTo(minimumValue) == 0) {
            return NEUTRAL_SCORE;
        }

        BigDecimal score;

        if (direction == ScoreDirection.HIGHER_IS_BETTER) {
            score = value
                    .subtract(minimumValue)
                    .divide(
                            maximumValue.subtract(minimumValue),
                            CALCULATION_SCALE,
                            RoundingMode.HALF_UP
                    )
                    .multiply(MAX_SCORE);
        } else {
            score = maximumValue
                    .subtract(value)
                    .divide(
                            maximumValue.subtract(minimumValue),
                            CALCULATION_SCALE,
                            RoundingMode.HALF_UP
                    )
                    .multiply(MAX_SCORE);
        }

        return score
                .max(MIN_SCORE)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}