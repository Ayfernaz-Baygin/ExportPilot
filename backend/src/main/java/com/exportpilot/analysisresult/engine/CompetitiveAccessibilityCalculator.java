package com.exportpilot.analysisresult.engine;

import com.exportpilot.trade.entity.TradeRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompetitiveAccessibilityCalculator {

    private static final String TURKEY_ISO2_CODE = "TR";

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final BigDecimal CONCENTRATION_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal TURKEY_RANK_WEIGHT =
            new BigDecimal("0.25");

    private static final BigDecimal LEADER_PROXIMITY_WEIGHT =
            new BigDecimal("0.25");

    private static final BigDecimal UNIT_VALUE_WEIGHT =
            new BigDecimal("0.20");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 8;

    public CompetitiveAccessibilityMetrics calculate(
            List<TradeRecord> supplierRecords,
            Integer analysisEndYear
    ) {
        if (
                supplierRecords == null
                        || supplierRecords.isEmpty()
                        || analysisEndYear == null
        ) {
            return emptyMetrics();
        }

        List<TradeRecord> latestYearRecords =
                supplierRecords.stream()
                        .filter(record ->
                                analysisEndYear.equals(
                                        record.getTradeYear()
                                )
                        )
                        .filter(record ->
                                record.getPartnerCountry() != null
                        )
                        .filter(record ->
                                record.getTradeValueUsd() != null
                        )
                        .filter(record ->
                                record.getTradeValueUsd()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .toList();

        if (latestYearRecords.isEmpty()) {
            return emptyMetrics();
        }

        Map<Long, SupplierData> supplierDataByCountryId =
                aggregateSupplierData(latestYearRecords);

        List<SupplierData> suppliers =
                supplierDataByCountryId.values()
                        .stream()
                        .filter(data ->
                                data.tradeValueUsd()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .sorted(
                                Comparator.comparing(
                                                SupplierData::tradeValueUsd
                                        )
                                        .reversed()
                        )
                        .toList();

        /*
         * Tek tedarikçi bulunuyorsa rekabet yapısı hakkında
         * anlamlı bir skor üretmek doğru olmaz.
         */
        if (suppliers.size() < 2) {
            return createMetricsWithoutScore(suppliers);
        }

        BigDecimal totalTradeValue =
                suppliers.stream()
                        .map(SupplierData::tradeValueUsd)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (totalTradeValue.compareTo(BigDecimal.ZERO) <= 0) {
            return emptyMetrics();
        }

        List<SupplierMarketData> supplierMarketData =
                calculateSupplierMarketData(
                        suppliers,
                        totalTradeValue
                );

        SupplierMarketData leader =
                supplierMarketData.getFirst();

        SupplierMarketData turkeySupplier =
                findTurkeySupplier(supplierMarketData);

        BigDecimal supplierConcentrationHhi =
                calculateHhi(supplierMarketData);

        BigDecimal concentrationScore =
                calculateLowConcentrationScore(
                        supplierConcentrationHhi
                );

        Integer turkeySupplierRank =
                turkeySupplier == null
                        ? null
                        : supplierMarketData.indexOf(
                                turkeySupplier
                        ) + 1;

        BigDecimal turkeyRankScore =
                calculateTurkeyRankScore(
                        turkeySupplierRank,
                        supplierMarketData.size()
                );

        BigDecimal turkeyMarketSharePercent =
                turkeySupplier == null
                        ? BigDecimal.ZERO.setScale(
                                SCORE_SCALE,
                                RoundingMode.HALF_UP
                        )
                        : turkeySupplier.marketSharePercent();

        BigDecimal leaderMarketSharePercent =
                leader.marketSharePercent();

        BigDecimal distanceToLeaderPercent =
                leaderMarketSharePercent
                        .subtract(turkeyMarketSharePercent)
                        .max(BigDecimal.ZERO)
                        .setScale(
                                SCORE_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal leaderProximityScore =
                calculateLeaderProximityScore(
                        turkeyMarketSharePercent,
                        leaderMarketSharePercent
                );

        BigDecimal turkeyUnitValue =
                turkeySupplier == null
                        ? null
                        : turkeySupplier.unitValueUsdPerKg();

        BigDecimal marketAverageUnitValue =
                calculateMarketAverageUnitValue(
                        supplierMarketData
                );

        BigDecimal unitValueScore =
                calculateUnitValueCompetitivenessScore(
                        turkeyUnitValue,
                        marketAverageUnitValue
                );

        BigDecimal overallScore =
                calculateWeightedScore(
                        concentrationScore,
                        turkeyRankScore,
                        leaderProximityScore,
                        unitValueScore
                );

        return new CompetitiveAccessibilityMetrics(
                overallScore,
                supplierMarketData.size(),
                supplierConcentrationHhi,
                turkeySupplierRank,
                turkeyMarketSharePercent,
                leaderMarketSharePercent,
                distanceToLeaderPercent,
                turkeyUnitValue,
                marketAverageUnitValue
        );
    }

    private Map<Long, SupplierData> aggregateSupplierData(
            List<TradeRecord> records
    ) {
        Map<Long, MutableSupplierData> grouped =
                new LinkedHashMap<>();

        for (TradeRecord record : records) {
            Long partnerCountryId =
                    record.getPartnerCountry().getId();

            MutableSupplierData supplierData =
                    grouped.computeIfAbsent(
                            partnerCountryId,
                            ignored ->
                                    new MutableSupplierData(
                                            record.getPartnerCountry()
                                                    .getIso2Code()
                                    )
                    );

            supplierData.tradeValueUsd =
                    supplierData.tradeValueUsd.add(
                            valueOrZero(
                                    record.getTradeValueUsd()
                            )
                    );

            supplierData.netWeightKg =
                    supplierData.netWeightKg.add(
                            valueOrZero(
                                    record.getNetWeightKg()
                            )
                    );
        }

        Map<Long, SupplierData> result =
                new LinkedHashMap<>();

        grouped.forEach((countryId, mutableData) ->
                result.put(
                        countryId,
                        new SupplierData(
                                mutableData.iso2Code,
                                mutableData.tradeValueUsd,
                                mutableData.netWeightKg
                        )
                )
        );

        return result;
    }

    private List<SupplierMarketData> calculateSupplierMarketData(
            List<SupplierData> suppliers,
            BigDecimal totalTradeValue
    ) {
        List<SupplierMarketData> result =
                new ArrayList<>();

        for (SupplierData supplier : suppliers) {
            BigDecimal marketSharePercent =
                    supplier.tradeValueUsd()
                            .divide(
                                    totalTradeValue,
                                    CALCULATION_SCALE,
                                    RoundingMode.HALF_UP
                            )
                            .multiply(
                                    new BigDecimal("100")
                            )
                            .setScale(
                                    SCORE_SCALE,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal unitValue =
                    calculateUnitValue(
                            supplier.tradeValueUsd(),
                            supplier.netWeightKg()
                    );

            result.add(
                    new SupplierMarketData(
                            supplier.iso2Code(),
                            supplier.tradeValueUsd(),
                            supplier.netWeightKg(),
                            marketSharePercent,
                            unitValue
                    )
            );
        }

        return result;
    }

    private SupplierMarketData findTurkeySupplier(
            List<SupplierMarketData> suppliers
    ) {
        return suppliers.stream()
                .filter(supplier ->
                        supplier.iso2Code() != null
                )
                .filter(supplier ->
                        TURKEY_ISO2_CODE.equalsIgnoreCase(
                                supplier.iso2Code()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calculateHhi(
            List<SupplierMarketData> suppliers
    ) {
        return suppliers.stream()
                .map(SupplierMarketData::marketSharePercent)
                .map(share ->
                        share.multiply(share)
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateLowConcentrationScore(
            BigDecimal hhi
    ) {
        if (hhi == null) {
            return null;
        }

        /*
         * HHI teorik olarak:
         * 0'a yaklaştıkça rekabetçi,
         * 10.000'e yaklaştıkça yoğunlaşmış pazardır.
         */
        return MAX_SCORE
                .subtract(
                        hhi.divide(
                                new BigDecimal("100"),
                                CALCULATION_SCALE,
                                RoundingMode.HALF_UP
                        )
                )
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateTurkeyRankScore(
            Integer turkeyRank,
            int supplierCount
    ) {
        if (
                turkeyRank == null
                        || supplierCount <= 0
        ) {
            return null;
        }

        if (supplierCount == 1) {
            return MAX_SCORE;
        }

        BigDecimal rankIndex =
                BigDecimal.valueOf(
                        turkeyRank - 1L
                );

        BigDecimal maximumIndex =
                BigDecimal.valueOf(
                        supplierCount - 1L
                );

        return MAX_SCORE
                .subtract(
                        rankIndex.divide(
                                        maximumIndex,
                                        CALCULATION_SCALE,
                                        RoundingMode.HALF_UP
                                )
                                .multiply(MAX_SCORE)
                )
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateLeaderProximityScore(
            BigDecimal turkeyShare,
            BigDecimal leaderShare
    ) {
        if (
                turkeyShare == null
                        || leaderShare == null
                        || leaderShare.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            return null;
        }

        return turkeyShare
                .divide(
                        leaderShare,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(MAX_SCORE)
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateUnitValue(
            BigDecimal tradeValueUsd,
            BigDecimal netWeightKg
    ) {
        if (
                tradeValueUsd == null
                        || netWeightKg == null
                        || netWeightKg.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            return null;
        }

        return tradeValueUsd
                .divide(
                        netWeightKg,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .setScale(
                        4,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateMarketAverageUnitValue(
            List<SupplierMarketData> suppliers
    ) {
        BigDecimal totalTradeValue =
                suppliers.stream()
                        .filter(supplier ->
                                supplier.netWeightKg()
                                        .compareTo(BigDecimal.ZERO) > 0
                        )
                        .map(SupplierMarketData::tradeValueUsd)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalNetWeight =
                suppliers.stream()
                        .map(SupplierMarketData::netWeightKg)
                        .filter(weight ->
                                weight.compareTo(
                                        BigDecimal.ZERO
                                ) > 0
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return calculateUnitValue(
                totalTradeValue,
                totalNetWeight
        );
    }

    private BigDecimal calculateUnitValueCompetitivenessScore(
            BigDecimal turkeyUnitValue,
            BigDecimal marketAverageUnitValue
    ) {
        if (
                turkeyUnitValue == null
                        || marketAverageUnitValue == null
                        || turkeyUnitValue.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            return null;
        }

        /*
         * Türkiye'nin birim değeri pazar ortalamasından düşükse
         * fiyat açısından daha rekabetçi kabul edilir.
         */
        return marketAverageUnitValue
                .divide(
                        turkeyUnitValue,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(MAX_SCORE)
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateWeightedScore(
            BigDecimal concentrationScore,
            BigDecimal turkeyRankScore,
            BigDecimal leaderProximityScore,
            BigDecimal unitValueScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (concentrationScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            concentrationScore.multiply(
                                    CONCENTRATION_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            CONCENTRATION_WEIGHT
                    );
        }

        if (turkeyRankScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            turkeyRankScore.multiply(
                                    TURKEY_RANK_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TURKEY_RANK_WEIGHT
                    );
        }

        if (leaderProximityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            leaderProximityScore.multiply(
                                    LEADER_PROXIMITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            LEADER_PROXIMITY_WEIGHT
                    );
        }

        if (unitValueScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            unitValueScore.multiply(
                                    UNIT_VALUE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            UNIT_VALUE_WEIGHT
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
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private CompetitiveAccessibilityMetrics createMetricsWithoutScore(
            List<SupplierData> suppliers
    ) {
        BigDecimal turkeyUnitValue =
                suppliers.stream()
                        .filter(supplier ->
                                supplier.iso2Code() != null
                        )
                        .filter(supplier ->
                                TURKEY_ISO2_CODE.equalsIgnoreCase(
                                        supplier.iso2Code()
                                )
                        )
                        .map(supplier ->
                                calculateUnitValue(
                                        supplier.tradeValueUsd(),
                                        supplier.netWeightKg()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        return new CompetitiveAccessibilityMetrics(
                null,
                suppliers.size(),
                null,
                null,
                null,
                null,
                null,
                turkeyUnitValue,
                null
        );
    }

    private CompetitiveAccessibilityMetrics emptyMetrics() {
        return new CompetitiveAccessibilityMetrics(
                null,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private BigDecimal valueOrZero(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private static class MutableSupplierData {

        private final String iso2Code;

        private BigDecimal tradeValueUsd =
                BigDecimal.ZERO;

        private BigDecimal netWeightKg =
                BigDecimal.ZERO;

        private MutableSupplierData(
                String iso2Code
        ) {
            this.iso2Code = iso2Code;
        }
    }

    private record SupplierData(

            String iso2Code,

            BigDecimal tradeValueUsd,

            BigDecimal netWeightKg

    ) {
    }

    private record SupplierMarketData(

            String iso2Code,

            BigDecimal tradeValueUsd,

            BigDecimal netWeightKg,

            BigDecimal marketSharePercent,

            BigDecimal unitValueUsdPerKg

    ) {
    }
}