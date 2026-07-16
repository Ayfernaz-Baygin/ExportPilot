package com.exportpilot.analysisresult.engine;

import com.exportpilot.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class TradeMetricsCalculator {

    private static final int MONEY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 4;

    public TradeMetrics calculate(List<YearlyTradeValue> yearlyValues) {
        if (yearlyValues == null || yearlyValues.isEmpty()) {
            throw new BusinessRuleException(
                    "At least one yearly trade value is required."
            );
        }

        List<YearlyTradeValue> validValues = yearlyValues.stream()
                .filter(Objects::nonNull)
                .filter(value -> value.year() != null)
                .filter(value -> value.tradeValueUsd() != null)
                .filter(value ->
                        value.tradeValueUsd()
                                .compareTo(BigDecimal.ZERO) >= 0
                )
                .sorted(Comparator.comparing(YearlyTradeValue::year))
                .toList();

        if (validValues.isEmpty()) {
            throw new BusinessRuleException(
                    "No valid yearly trade values were found."
            );
        }

        YearlyTradeValue firstValue = validValues.getFirst();
        YearlyTradeValue lastValue = validValues.getLast();

        BigDecimal totalTradeValue = validValues.stream()
                .map(YearlyTradeValue::tradeValueUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTradeValue = totalTradeValue.divide(
                BigDecimal.valueOf(validValues.size()),
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );

        BigDecimal absoluteGrowth = lastValue.tradeValueUsd()
                .subtract(firstValue.tradeValueUsd());

        BigDecimal growthRatePercent = calculateGrowthRate(
                firstValue.tradeValueUsd(),
                lastValue.tradeValueUsd()
        );

        BigDecimal cagrPercent = calculateCagr(
                firstValue.year(),
                lastValue.year(),
                firstValue.tradeValueUsd(),
                lastValue.tradeValueUsd()
        );

        return new TradeMetrics(
                firstValue.year(),
                lastValue.year(),
                validValues.size(),

                firstValue.tradeValueUsd()
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP),

                lastValue.tradeValueUsd()
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP),

                totalTradeValue
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP),

                averageTradeValue,

                absoluteGrowth
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP),

                growthRatePercent,
                cagrPercent
        );
    }

    private BigDecimal calculateGrowthRate(
            BigDecimal firstValue,
            BigDecimal lastValue
    ) {
        if (firstValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return lastValue
                .subtract(firstValue)
                .divide(
                        firstValue,
                        PERCENTAGE_SCALE + 4,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateCagr(
            Integer firstYear,
            Integer lastYear,
            BigDecimal firstValue,
            BigDecimal lastValue
    ) {
        int yearDifference = lastYear - firstYear;

        if (yearDifference <= 0) {
            return BigDecimal.ZERO
                    .setScale(
                            PERCENTAGE_SCALE,
                            RoundingMode.HALF_UP
                    );
        }

        if (firstValue.compareTo(BigDecimal.ZERO) <= 0
                || lastValue.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        double ratio = lastValue
                .divide(
                        firstValue,
                        12,
                        RoundingMode.HALF_UP
                )
                .doubleValue();

        double cagr = (
                Math.pow(ratio, 1.0 / yearDifference) - 1.0
        ) * 100.0;

        if (!Double.isFinite(cagr)) {
            return null;
        }

        return BigDecimal.valueOf(cagr)
                .setScale(
                        PERCENTAGE_SCALE,
                        RoundingMode.HALF_UP
                );
    }
}