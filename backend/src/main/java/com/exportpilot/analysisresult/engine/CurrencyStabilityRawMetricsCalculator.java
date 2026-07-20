package com.exportpilot.analysisresult.engine;

import com.exportpilot.economicindicator.entity.EconomicIndicator;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class CurrencyStabilityRawMetricsCalculator {

    private static final int CALCULATION_SCALE = 10;

    private static final int METRIC_SCALE = 4;

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    public CurrencyStabilityRawMetrics calculate(
            Long countryId,
            List<EconomicIndicator> indicators,
            Integer startYear,
            Integer endYear
    ) {
        if (countryId == null
                || indicators == null
                || startYear == null
                || endYear == null
                || startYear > endYear) {
            return emptyMetrics(countryId, startYear, endYear);
        }

        List<EconomicIndicator> exchangeRateIndicators =
                indicators.stream()
                        .filter(indicator ->
                                indicator != null
                        )
                        .filter(indicator ->
                                indicator.getIndicatorType()
                                        == EconomicIndicatorType.EXCHANGE_RATE
                        )
                        .filter(indicator ->
                                indicator.getYear() != null
                        )
                        .filter(indicator ->
                                indicator.getYear() >= startYear
                                        && indicator.getYear() <= endYear
                        )
                        .filter(indicator ->
                                indicator.getValue() != null
                        )
                        .filter(indicator ->
                                indicator.getValue()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .sorted(
                                Comparator.comparing(
                                        EconomicIndicator::getYear
                                )
                        )
                        .toList();

        int expectedExchangeRateCount =
                endYear - startYear + 1;

        if (exchangeRateIndicators.size() < 2) {
            return new CurrencyStabilityRawMetrics(
                    countryId,
                    null,
                    null,
                    null,
                    exchangeRateIndicators.size(),
                    expectedExchangeRateCount
            );
        }

        List<BigDecimal> yearlyChangePercentages =
                calculateYearlyChangePercentages(
                        exchangeRateIndicators
                );

        BigDecimal averageAbsoluteChangePercent =
                calculateAverageAbsoluteChange(
                        yearlyChangePercentages
                );

        BigDecimal volatilityPercent =
                calculateStandardDeviation(
                        yearlyChangePercentages
                );

        BigDecimal cumulativeChangePercent =
                calculateCumulativeChange(
                        exchangeRateIndicators
                );

        return new CurrencyStabilityRawMetrics(
                countryId,
                averageAbsoluteChangePercent,
                volatilityPercent,
                cumulativeChangePercent,
                exchangeRateIndicators.size(),
                expectedExchangeRateCount
        );
    }

    private List<BigDecimal> calculateYearlyChangePercentages(
            List<EconomicIndicator> exchangeRateIndicators
    ) {
        List<BigDecimal> yearlyChanges =
                new ArrayList<>();

        for (
                int index = 1;
                index < exchangeRateIndicators.size();
                index++
        ) {
            BigDecimal previousValue =
                    exchangeRateIndicators
                            .get(index - 1)
                            .getValue();

            BigDecimal currentValue =
                    exchangeRateIndicators
                            .get(index)
                            .getValue();

            if (previousValue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal changePercent =
                    currentValue
                            .subtract(previousValue)
                            .divide(
                                    previousValue,
                                    CALCULATION_SCALE,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(ONE_HUNDRED)
                            .setScale(
                                    METRIC_SCALE,
                                    RoundingMode.HALF_UP
                            );

            yearlyChanges.add(changePercent);
        }

        return yearlyChanges;
    }

    private BigDecimal calculateAverageAbsoluteChange(
            List<BigDecimal> yearlyChanges
    ) {
        if (yearlyChanges == null || yearlyChanges.isEmpty()) {
            return null;
        }

        BigDecimal absoluteChangeSum =
                yearlyChanges.stream()
                        .map(BigDecimal::abs)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return absoluteChangeSum
                .divide(
                        BigDecimal.valueOf(
                                yearlyChanges.size()
                        ),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .setScale(
                        METRIC_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateStandardDeviation(
            List<BigDecimal> yearlyChanges
    ) {
        if (yearlyChanges == null
                || yearlyChanges.size() < 2) {
            return null;
        }

        BigDecimal average =
                yearlyChanges.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        yearlyChanges.size()
                                ),
                                CALCULATION_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal squaredDifferenceSum =
                yearlyChanges.stream()
                        .map(change ->
                                change.subtract(average)
                        )
                        .map(difference ->
                                difference.multiply(difference)
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal variance =
                squaredDifferenceSum.divide(
                        BigDecimal.valueOf(
                                yearlyChanges.size()
                        ),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                );

        double standardDeviation =
                Math.sqrt(
                        variance.doubleValue()
                );

        return BigDecimal
                .valueOf(standardDeviation)
                .setScale(
                        METRIC_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateCumulativeChange(
            List<EconomicIndicator> exchangeRateIndicators
    ) {
        BigDecimal firstValue =
                exchangeRateIndicators
                        .getFirst()
                        .getValue();

        BigDecimal lastValue =
                exchangeRateIndicators
                        .getLast()
                        .getValue();

        if (firstValue.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return lastValue
                .subtract(firstValue)
                .divide(
                        firstValue,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(ONE_HUNDRED)
                .setScale(
                        METRIC_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private CurrencyStabilityRawMetrics emptyMetrics(
            Long countryId,
            Integer startYear,
            Integer endYear
    ) {
        Integer expectedCount =
                startYear == null
                        || endYear == null
                        || startYear > endYear
                        ? 0
                        : endYear - startYear + 1;

        return new CurrencyStabilityRawMetrics(
                countryId,
                null,
                null,
                null,
                0,
                expectedCount
        );
    }
}