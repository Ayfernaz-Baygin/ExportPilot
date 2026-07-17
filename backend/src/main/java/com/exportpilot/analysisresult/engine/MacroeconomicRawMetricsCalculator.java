package com.exportpilot.analysisresult.engine;

import com.exportpilot.economicindicator.entity.EconomicDataStatus;
import com.exportpilot.economicindicator.entity.EconomicIndicator;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class MacroeconomicRawMetricsCalculator {

    private static final int EXPECTED_COMPONENT_COUNT = 6;

    private static final int CALCULATION_SCALE = 10;

    public MacroeconomicRawMetrics calculate(
            Long countryId,
            List<EconomicIndicator> indicators,
            Integer analysisStartYear,
            Integer analysisEndYear
    ) {
        List<EconomicIndicator> safeIndicators =
                indicators == null
                        ? List.of()
                        : indicators;

        BigDecimal gdpGrowth =
                findLatestUsableValue(
                        safeIndicators,
                        EconomicIndicatorType.GDP_GROWTH,
                        analysisEndYear
                );

        BigDecimal inflation =
                findLatestUsableValue(
                        safeIndicators,
                        EconomicIndicatorType.INFLATION,
                        analysisEndYear
                );

        BigDecimal unemployment =
                findLatestUsableValue(
                        safeIndicators,
                        EconomicIndicatorType.UNEMPLOYMENT,
                        analysisEndYear
                );

        BigDecimal gdpPerCapita =
                findLatestUsableValue(
                        safeIndicators,
                        EconomicIndicatorType.GDP_PER_CAPITA,
                        analysisEndYear
                );

        BigDecimal tradeGdpRatio =
                findLatestUsableValue(
                        safeIndicators,
                        EconomicIndicatorType.TRADE_GDP_RATIO,
                        analysisEndYear
                );

        BigDecimal gdpGrowthVolatility =
                calculateGdpGrowthVolatility(
                        safeIndicators,
                        analysisStartYear,
                        analysisEndYear
                );

        int availableComponentCount =
                countNonNullValues(
                        gdpGrowth,
                        inflation,
                        unemployment,
                        gdpPerCapita,
                        tradeGdpRatio,
                        gdpGrowthVolatility
                );

        return new MacroeconomicRawMetrics(
                countryId,
                gdpGrowth,
                inflation,
                unemployment,
                gdpPerCapita,
                tradeGdpRatio,
                gdpGrowthVolatility,
                availableComponentCount,
                EXPECTED_COMPONENT_COUNT
        );
    }

    private BigDecimal findLatestUsableValue(
            List<EconomicIndicator> indicators,
            EconomicIndicatorType indicatorType,
            Integer analysisEndYear
    ) {
        return indicators.stream()
                .filter(Objects::nonNull)
                .filter(indicator ->
                        indicator.getIndicatorType() == indicatorType
                )
                .filter(indicator ->
                        indicator.getYear() != null
                )
                .filter(indicator ->
                        analysisEndYear == null
                                || indicator.getYear() <= analysisEndYear
                )
                .filter(this::isUsable)
                .sorted(
                        Comparator.comparing(
                                EconomicIndicator::getYear
                        ).reversed()
                )
                .map(EconomicIndicator::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calculateGdpGrowthVolatility(
            List<EconomicIndicator> indicators,
            Integer analysisStartYear,
            Integer analysisEndYear
    ) {
        List<BigDecimal> growthValues =
                indicators.stream()
                        .filter(Objects::nonNull)
                        .filter(indicator ->
                                indicator.getIndicatorType()
                                        == EconomicIndicatorType.GDP_GROWTH
                        )
                        .filter(indicator ->
                                indicator.getYear() != null
                        )
                        .filter(indicator ->
                                analysisStartYear == null
                                        || indicator.getYear()
                                        >= analysisStartYear
                        )
                        .filter(indicator ->
                                analysisEndYear == null
                                        || indicator.getYear()
                                        <= analysisEndYear
                        )
                        .filter(this::isUsable)
                        .map(EconomicIndicator::getValue)
                        .filter(Objects::nonNull)
                        .toList();

        if (growthValues.size() < 2) {
            return null;
        }

        BigDecimal sum =
                growthValues.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal mean =
                sum.divide(
                        BigDecimal.valueOf(growthValues.size()),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal squaredDifferenceSum =
                growthValues.stream()
                        .map(value ->
                                value.subtract(mean)
                                        .pow(
                                                2,
                                                MathContext.DECIMAL128
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal variance =
                squaredDifferenceSum.divide(
                        BigDecimal.valueOf(growthValues.size()),
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                );

        double squareRoot =
                Math.sqrt(variance.doubleValue());

        return BigDecimal.valueOf(squareRoot)
                .setScale(
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private boolean isUsable(
            EconomicIndicator indicator
    ) {
        if (indicator.getValue() == null) {
            return false;
        }

        EconomicDataStatus status =
                indicator.getDataStatus();

        return status == EconomicDataStatus.AVAILABLE
                || status == EconomicDataStatus.PARTIAL
                || status == EconomicDataStatus.STALE;
    }

    private int countNonNullValues(
            BigDecimal... values
    ) {
        int count = 0;

        for (BigDecimal value : values) {
            if (value != null) {
                count++;
            }
        }

        return count;
    }
}