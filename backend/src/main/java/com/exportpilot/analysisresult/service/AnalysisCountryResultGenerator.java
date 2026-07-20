package com.exportpilot.analysisresult.service;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.entity.AnalysisStatus;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.analysisresult.engine.CompetitiveAccessibilityCalculator;
import com.exportpilot.analysisresult.engine.CompetitiveAccessibilityMetrics;
import com.exportpilot.analysisresult.engine.CurrencyStabilityRawMetrics;
import com.exportpilot.analysisresult.engine.CurrencyStabilityRawMetricsCalculator;
import com.exportpilot.analysisresult.engine.CurrencyStabilityCalculator;
import com.exportpilot.analysisresult.engine.CurrencyStabilityMetrics;
import com.exportpilot.analysisresult.engine.LogisticsDataProvider;
import com.exportpilot.analysisresult.engine.LogisticsRawData;
import com.exportpilot.analysisresult.engine.LogisticsSuitabilityCalculator;
import com.exportpilot.analysisresult.engine.LogisticsSuitabilityMetrics;
import com.exportpilot.analysisresult.engine.TariffDataProvider;
import com.exportpilot.analysisresult.engine.TariffRawData;
import com.exportpilot.analysisresult.engine.TariffSuitabilityCalculator;
import com.exportpilot.analysisresult.engine.TariffSuitabilityMetrics;
import com.exportpilot.analysisresult.engine.MacroeconomicMetrics;
import com.exportpilot.analysisresult.engine.MacroeconomicRawMetrics;
import com.exportpilot.analysisresult.engine.MacroeconomicRawMetricsCalculator;
import com.exportpilot.analysisresult.engine.MacroeconomicStabilityCalculator;
import com.exportpilot.analysisresult.engine.TradeMetrics;
import com.exportpilot.analysisresult.engine.TradeMetricsCalculator;
import com.exportpilot.analysisresult.engine.YearlyTradeValue;
import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import com.exportpilot.analysisresult.repository.AnalysisCountryResultRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.economicindicator.entity.EconomicIndicator;
import com.exportpilot.economicindicator.repository.EconomicIndicatorRepository;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradePartnerScope;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalysisCountryResultGenerator {

    private static final String TURKEY_ISO2_CODE = "TR";

    /*
     * Geçici beş bileşenli skor modeli.
     *
     * Diğer skorlar tamamlandığında nihai ağırlık sistemine geçilecek:
     *
     * Import Market Size           %20
     * Import Growth                %20
     * Türkiye Export Performance   %15
     * Market Share Opportunity     %10
     * Competitive Accessibility    %10
     * Macroeconomic Stability      %10
     * Currency Stability            %5
     * Logistics Suitability         %5
     * Tariff Suitability            %5
     */
    private static final BigDecimal IMPORT_MARKET_SIZE_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal IMPORT_GROWTH_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal TURKEY_EXPORT_PERFORMANCE_WEIGHT =
            new BigDecimal("0.15");

    private static final BigDecimal MARKET_SHARE_OPPORTUNITY_WEIGHT =
            new BigDecimal("0.10");

    private static final BigDecimal COMPETITIVE_ACCESSIBILITY_WEIGHT =
            new BigDecimal("0.10");

    private static final BigDecimal MACROECONOMIC_STABILITY_WEIGHT =
            new BigDecimal("0.10");

    private static final BigDecimal CURRENCY_STABILITY_WEIGHT =
            new BigDecimal("0.05");

    private static final BigDecimal LOGISTICS_SUITABILITY_WEIGHT =
            new BigDecimal("0.05");

    private static final BigDecimal TARIFF_SUITABILITY_WEIGHT =
            new BigDecimal("0.05");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private static final int CALCULATION_SCALE = 8;

    private final AnalysisRepository analysisRepository;

    private final AnalysisCountryResultRepository resultRepository;

    private final TradeRecordRepository tradeRecordRepository;

    private final TradeMetricsCalculator metricsCalculator;

    private final CompetitiveAccessibilityCalculator
            competitiveAccessibilityCalculator;

    private final EconomicIndicatorRepository economicIndicatorRepository;

    private final MacroeconomicRawMetricsCalculator
            macroeconomicRawMetricsCalculator;

    private final MacroeconomicStabilityCalculator
            macroeconomicStabilityCalculator;

    private final CurrencyStabilityRawMetricsCalculator
            currencyStabilityRawMetricsCalculator;

    private final CurrencyStabilityCalculator
            currencyStabilityCalculator;

    private final LogisticsDataProvider logisticsDataProvider;

    private final LogisticsSuitabilityCalculator
            logisticsSuitabilityCalculator;

    private final TariffDataProvider tariffDataProvider;

    private final TariffSuitabilityCalculator
            tariffSuitabilityCalculator;

    public AnalysisCountryResultGenerator(
            AnalysisRepository analysisRepository,
            AnalysisCountryResultRepository resultRepository,
            TradeRecordRepository tradeRecordRepository,
            TradeMetricsCalculator metricsCalculator,
            CompetitiveAccessibilityCalculator
                    competitiveAccessibilityCalculator,
            EconomicIndicatorRepository economicIndicatorRepository,
            MacroeconomicRawMetricsCalculator
                    macroeconomicRawMetricsCalculator,
            MacroeconomicStabilityCalculator
                    macroeconomicStabilityCalculator,
            CurrencyStabilityRawMetricsCalculator
                    currencyStabilityRawMetricsCalculator,
            CurrencyStabilityCalculator
                    currencyStabilityCalculator,
            LogisticsDataProvider logisticsDataProvider,
            LogisticsSuitabilityCalculator
                    logisticsSuitabilityCalculator,
            TariffDataProvider tariffDataProvider,
            TariffSuitabilityCalculator
                    tariffSuitabilityCalculator
    ) {
        this.analysisRepository = analysisRepository;
        this.resultRepository = resultRepository;
        this.tradeRecordRepository = tradeRecordRepository;
        this.metricsCalculator = metricsCalculator;
        this.competitiveAccessibilityCalculator =
                competitiveAccessibilityCalculator;
        this.economicIndicatorRepository =
                economicIndicatorRepository;
        this.macroeconomicRawMetricsCalculator =
                macroeconomicRawMetricsCalculator;
        this.macroeconomicStabilityCalculator =
                macroeconomicStabilityCalculator;
        this.currencyStabilityRawMetricsCalculator =
                currencyStabilityRawMetricsCalculator;
        this.currencyStabilityCalculator =
                currencyStabilityCalculator;
        this.logisticsDataProvider = logisticsDataProvider;
        this.logisticsSuitabilityCalculator =
                logisticsSuitabilityCalculator;
        this.tariffDataProvider = tariffDataProvider;
        this.tariffSuitabilityCalculator =
                tariffSuitabilityCalculator;
    }

    @Transactional
    public List<AnalysisCountryResult> generateResults(
            Long analysisId
    ) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis not found with id: "
                                        + analysisId
                        )
                );

        analysis.setStatus(AnalysisStatus.RUNNING);
        analysis.setCompletedAt(null);
        analysisRepository.save(analysis);

        try {
            resultRepository.deleteAllByAnalysisId(analysisId);
            resultRepository.flush();

            List<TradeRecord> worldTotalRecords =
                    getWorldTotalRecords(analysis);

            List<TradeRecord> turkeyRecords =
                    getTurkeyPartnerRecords(analysis);

            List<TradeRecord> supplierRecords =
                    getSupplierRecords(analysis);

            if (worldTotalRecords.isEmpty()) {
                throw new BusinessRuleException(
                        "No WORLD_TOTAL trade records were found "
                                + "for analysis id: "
                                + analysisId
                );
            }

            List<CountryCandidate> candidates =
                    createCountryCandidates(
                            analysis,
                            worldTotalRecords,
                            turkeyRecords,
                            supplierRecords
                    );

            List<CountryCandidate> candidatesWithEconomicData =
                    enrichCandidatesWithEconomicData(
                            analysis,
                            candidates
                    );

            List<AnalysisCountryResult> results =
                    createRankedResults(
                            analysis,
                            candidatesWithEconomicData
                    );

            List<AnalysisCountryResult> savedResults =
                    resultRepository.saveAll(results);

            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(OffsetDateTime.now());
            analysisRepository.save(analysis);

            return savedResults;
        } catch (RuntimeException exception) {
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setCompletedAt(OffsetDateTime.now());
            analysisRepository.save(analysis);

            throw exception;
        }
    }

    private List<TradeRecord> getWorldTotalRecords(
            Analysis analysis
    ) {
        List<TradeRecord> records =
                tradeRecordRepository
                        .findAllByProductCodeIdAndPartnerScopeAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
                                analysis.getProductCode().getId(),
                                TradePartnerScope.WORLD_TOTAL,
                                TradeFlow.IMPORT,
                                analysis.getStartYear(),
                                analysis.getEndYear()
                        );

        return filterByTargetRegion(
                records,
                analysis.getTargetRegion()
        );
    }

    private List<TradeRecord> getTurkeyPartnerRecords(
            Analysis analysis
    ) {
        List<TradeRecord> records =
                tradeRecordRepository
                        .findAllByProductCodeIdAndPartnerScopeAndPartnerCountryIso2CodeAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
                                analysis.getProductCode().getId(),
                                TradePartnerScope.SPECIFIC_COUNTRY,
                                TURKEY_ISO2_CODE,
                                TradeFlow.IMPORT,
                                analysis.getStartYear(),
                                analysis.getEndYear()
                        );

        return filterByTargetRegion(
                records,
                analysis.getTargetRegion()
        );
    }

    private List<TradeRecord> getSupplierRecords(
            Analysis analysis
    ) {
        List<TradeRecord> records =
                tradeRecordRepository
                        .findAllByProductCodeIdAndPartnerScopeAndTradeFlowAndTradeYearBetweenAndPartnerCountryIsNotNullOrderByTradeYearAsc(
                                analysis.getProductCode().getId(),
                                TradePartnerScope.SPECIFIC_COUNTRY,
                                TradeFlow.IMPORT,
                                analysis.getStartYear(),
                                analysis.getEndYear()
                        );

        return filterByTargetRegion(
                records,
                analysis.getTargetRegion()
        );
    }

    private List<TradeRecord> filterByTargetRegion(
            List<TradeRecord> records,
            String targetRegion
    ) {
        if (targetRegion == null || targetRegion.isBlank()) {
            return records;
        }

        String normalizedTargetRegion =
                targetRegion.trim();

        return records.stream()
                .filter(record ->
                        record.getReporterCountry() != null
                )
                .filter(record ->
                        record.getReporterCountry().getRegion() != null
                )
                .filter(record ->
                        record.getReporterCountry()
                                .getRegion()
                                .equalsIgnoreCase(
                                        normalizedTargetRegion
                                )
                )
                .toList();
    }

    private List<CountryCandidate> createCountryCandidates(
            Analysis analysis,
            List<TradeRecord> worldTotalRecords,
            List<TradeRecord> turkeyRecords,
            List<TradeRecord> supplierRecords
    ) {
        Map<Long, List<TradeRecord>> worldRecordsByCountryId =
                worldTotalRecords.stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.getReporterCountry()
                                                        .getId(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        Map<Long, List<TradeRecord>> turkeyRecordsByCountryId =
                turkeyRecords.stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.getReporterCountry()
                                                        .getId(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        Map<Long, List<TradeRecord>> supplierRecordsByCountryId =
                supplierRecords.stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.getReporterCountry()
                                                        .getId(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        return worldRecordsByCountryId.entrySet()
                .stream()
                .map(entry -> {
                    List<TradeRecord> countryWorldRecords =
                            entry.getValue();

                    Country country =
                            countryWorldRecords
                                    .getFirst()
                                    .getReporterCountry();

                    List<TradeRecord> countryTurkeyRecords =
                            turkeyRecordsByCountryId.getOrDefault(
                                    entry.getKey(),
                                    List.of()
                            );

                    TradeMetrics worldMetrics =
                            calculateMetrics(
                                    countryWorldRecords
                            );

                    TradeMetrics turkeyMetrics =
                            calculateMetrics(
                                    countryTurkeyRecords
                            );

                    BigDecimal turkeyMarketSharePercent =
                            calculateLatestYearTurkeyMarketShare(
                                    analysis,
                                    countryWorldRecords,
                                    countryTurkeyRecords
                            );

                    List<TradeRecord> countrySupplierRecords =
                            supplierRecordsByCountryId.getOrDefault(
                                    entry.getKey(),
                                    List.of()
                            );

                    CompetitiveAccessibilityMetrics
                            competitiveAccessibilityMetrics =
                            competitiveAccessibilityCalculator.calculate(
                                    countrySupplierRecords,
                                    analysis.getEndYear()
                            );

                    return new CountryCandidate(
                            country,
                            worldMetrics,
                            turkeyMetrics,
                            turkeyMarketSharePercent,
                            competitiveAccessibilityMetrics,
                            null,
                            null
                    );
                })
                .toList();
    }

    private List<CountryCandidate> enrichCandidatesWithEconomicData(
            Analysis analysis,
            List<CountryCandidate> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<Long> countryIds =
                candidates.stream()
                        .map(candidate ->
                                candidate.country().getId()
                        )
                        .distinct()
                        .toList();

        List<EconomicIndicator> economicIndicators =
                economicIndicatorRepository
                        .findAllByCountryIdInAndYearBetweenOrderByCountryIdAscIndicatorTypeAscYearAsc(
                                countryIds,
                                analysis.getStartYear(),
                                analysis.getEndYear()
                        );

        Map<Long, List<EconomicIndicator>>
                indicatorsByCountryId =
                economicIndicators.stream()
                        .collect(
                                Collectors.groupingBy(
                                        indicator ->
                                                indicator.getCountry()
                                                        .getId(),
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        return candidates.stream()
                .map(candidate -> {
                    Long countryId =
                            candidate.country().getId();

                    List<EconomicIndicator> countryIndicators =
                            indicatorsByCountryId.getOrDefault(
                                    countryId,
                                    List.of()
                            );

                    MacroeconomicRawMetrics rawMetrics =
                            macroeconomicRawMetricsCalculator
                                    .calculate(
                                            countryId,
                                            countryIndicators,
                                            analysis.getStartYear(),
                                            analysis.getEndYear()
                                    );

                    CurrencyStabilityRawMetrics
                            currencyStabilityRawMetrics =
                            currencyStabilityRawMetricsCalculator
                                    .calculate(
                                            countryId,
                                            countryIndicators,
                                            analysis.getStartYear(),
                                            analysis.getEndYear()
                                    );

                    return new CountryCandidate(
                            candidate.country(),
                            candidate.worldMetrics(),
                            candidate.turkeyMetrics(),
                            candidate.turkeyMarketSharePercent(),
                            candidate.competitiveAccessibilityMetrics(),
                            rawMetrics,
                            currencyStabilityRawMetrics
                    );
                })
                .toList();
    }

    private TradeMetrics calculateMetrics(
            List<TradeRecord> records
    ) {
        Map<Integer, BigDecimal> yearlyTotals =
                records.stream()
                        .filter(record ->
                                record.getTradeYear() != null
                        )
                        .filter(record ->
                                record.getTradeValueUsd() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        TradeRecord::getTradeYear,
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                TradeRecord::getTradeValueUsd,
                                                BigDecimal::add
                                        )
                                )
                        );

        List<YearlyTradeValue> yearlyValues =
                yearlyTotals.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry ->
                                new YearlyTradeValue(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        return metricsCalculator.calculate(yearlyValues);
    }

    private BigDecimal calculateLatestYearTurkeyMarketShare(
            Analysis analysis,
            List<TradeRecord> worldRecords,
            List<TradeRecord> turkeyRecords
    ) {
        BigDecimal latestWorldValue =
                getTradeValueForYear(
                        worldRecords,
                        analysis.getEndYear()
                );

        BigDecimal latestTurkeyValue =
                getTradeValueForYear(
                        turkeyRecords,
                        analysis.getEndYear()
                );

        if (latestWorldValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return latestTurkeyValue
                .divide(
                        latestWorldValue,
                        CALCULATION_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(new BigDecimal("100"))
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal getTradeValueForYear(
            List<TradeRecord> records,
            Integer year
    ) {
        return records.stream()
                .filter(record ->
                        year.equals(record.getTradeYear())
                )
                .map(TradeRecord::getTradeValueUsd)
                .filter(value ->
                        value != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private List<AnalysisCountryResult> createRankedResults(
            Analysis analysis,
            List<CountryCandidate> candidates
    ) {
        List<MacroeconomicRawMetrics>
                allMacroeconomicRawMetrics =
                candidates.stream()
                        .map(
                                CountryCandidate::
                                        macroeconomicRawMetrics
                        )
                        .filter(value ->
                                value != null
                        )
                        .toList();

        List<CurrencyStabilityRawMetrics>
                allCurrencyStabilityRawMetrics =
                candidates.stream()
                        .map(
                                CountryCandidate::
                                        currencyStabilityRawMetrics
                        )
                        .filter(value ->
                                value != null
                        )
                        .toList();

        BigDecimal maximumWorldTotalTradeValue =
                getMaximumValue(
                        candidates,
                        candidate ->
                                candidate.worldMetrics()
                                        .totalTradeValueUsd()
                );

        List<BigDecimal> worldCagrValues =
                candidates.stream()
                        .map(candidate ->
                                candidate.worldMetrics()
                                        .cagrPercent()
                        )
                        .filter(value ->
                                value != null
                        )
                        .toList();

        BigDecimal minimumWorldCagr =
                worldCagrValues.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal maximumWorldCagr =
                worldCagrValues.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal maximumTurkeyExportPerformanceMetric =
                getMaximumValue(
                        candidates,
                        candidate ->
                                getTurkeyExportPerformanceMetric(
                                        candidate.turkeyMetrics()
                                )
                );

        BigDecimal maximumTurkeyMarketShare =
                getMaximumValue(
                        candidates,
                        CountryCandidate::turkeyMarketSharePercent
                );

        List<ScoredCandidate> scoredCandidates =
                candidates.stream()
                        .map(candidate ->
                                scoreCandidate(
                                        candidate,
                                        allMacroeconomicRawMetrics,
                                        allCurrencyStabilityRawMetrics,
                                        maximumWorldTotalTradeValue,
                                        minimumWorldCagr,
                                        maximumWorldCagr,
                                        maximumTurkeyExportPerformanceMetric,
                                        maximumTurkeyMarketShare
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                                ScoredCandidate::overallScore
                                        )
                                        .reversed()
                                        .thenComparing(
                                                candidate ->
                                                        candidate.country()
                                                                .getName()
                                        )
                        )
                        .limit(analysis.getMaxCountries())
                        .toList();

        List<AnalysisCountryResult> results =
                new ArrayList<>();

        OffsetDateTime calculationTime =
                OffsetDateTime.now();

        for (
                int index = 0;
                index < scoredCandidates.size();
                index++
        ) {
            ScoredCandidate candidate =
                    scoredCandidates.get(index);

            TradeMetrics worldMetrics =
                    candidate.worldMetrics();

            AnalysisCountryResult result =
                    AnalysisCountryResult.builder()
                            .analysis(analysis)
                            .country(candidate.country())
                            .rankPosition(index + 1)
                            .overallScore(
                                    candidate.overallScore()
                            )
                            .importMarketSizeScore(
                                    candidate.importMarketSizeScore()
                            )
                            .importGrowthScore(
                                    candidate.importGrowthScore()
                            )
                            .turkeyExportPerformanceScore(
                                    candidate
                                            .turkeyExportPerformanceScore()
                            )
                            .marketShareOpportunityScore(
                                    candidate
                                            .marketShareOpportunityScore()
                            )
                            .competitiveAccessibilityScore(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .competitiveAccessibilityScore()
                            )
                            .supplierCount(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .supplierCount()
                            )
                            .supplierConcentrationHhi(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .supplierConcentrationHhi()
                            )
                            .turkeySupplierRank(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .turkeySupplierRank()
                            )
                            .turkeyMarketSharePercent(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .turkeyMarketSharePercent()
                            )
                            .leaderMarketSharePercent(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .leaderMarketSharePercent()
                            )
                            .distanceToLeaderPercent(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .distanceToLeaderPercent()
                            )
                            .turkeyUnitValueUsdPerKg(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .turkeyUnitValueUsdPerKg()
                            )
                            .marketAverageUnitValueUsdPerKg(
                                    candidate
                                            .competitiveAccessibilityMetrics()
                                            .marketAverageUnitValueUsdPerKg()
                            )
                            .macroeconomicStabilityScore(
                                    candidate
                                            .macroeconomicStabilityScore()
                            )
                            .currencyStabilityScore(
                                    candidate.currencyStabilityScore()
                            )
                            .logisticsSuitabilityScore(
                                    candidate
                                            .logisticsSuitabilityMetrics()
                                            .logisticsSuitabilityScore()
                            )
                            .tariffSuitabilityScore(
                                    candidate
                                            .tariffSuitabilityMetrics()
                                            .tariffSuitabilityScore()
                            )
                            .dataCompleteness(
                                    calculateDataCompleteness(
                                            analysis,
                                            worldMetrics
                                    )
                            )
                            .firstYear(
                                    worldMetrics.firstYear()
                            )
                            .lastYear(
                                    worldMetrics.lastYear()
                            )
                            .availableYearCount(
                                    worldMetrics.availableYearCount()
                            )
                            .firstYearTradeValueUsd(
                                    worldMetrics
                                            .firstYearTradeValueUsd()
                            )
                            .lastYearTradeValueUsd(
                                    worldMetrics
                                            .lastYearTradeValueUsd()
                            )
                            .totalTradeValueUsd(
                                    worldMetrics.totalTradeValueUsd()
                            )
                            .averageTradeValueUsd(
                                    worldMetrics.averageTradeValueUsd()
                            )
                            .absoluteGrowthUsd(
                                    worldMetrics.absoluteGrowthUsd()
                            )
                            .growthRatePercent(
                                    worldMetrics.growthRatePercent()
                            )
                            .cagrPercent(
                                    worldMetrics.cagrPercent()
                            )
                            .calculatedAt(calculationTime)
                            .build();

            results.add(result);
        }

        return results;
    }

    private BigDecimal getTurkeyExportPerformanceMetric(
            TradeMetrics turkeyMetrics
    ) {
        if (turkeyMetrics == null) {
            return null;
        }

        int availableYearCount =
                turkeyMetrics.availableYearCount();

        /*
         * En az 3 yıllık veri varsa CAGR kullanılır.
         */
        if (availableYearCount >= 3
                && turkeyMetrics.cagrPercent() != null) {
            return turkeyMetrics
                    .cagrPercent()
                    .max(BigDecimal.ZERO);
        }

        /*
         * Yalnızca 2 yıllık veri varsa toplam büyüme oranı kullanılır.
         */
        if (availableYearCount >= 2
                && turkeyMetrics.growthRatePercent() != null) {
            return turkeyMetrics
                    .growthRatePercent()
                    .max(BigDecimal.ZERO);
        }

        /*
         * Tek yıllık veriyle ihracat büyümesi hesaplanamaz.
         */
        return null;
    }

    private ScoredCandidate scoreCandidate(
            CountryCandidate candidate,
            List<MacroeconomicRawMetrics>
                    allMacroeconomicRawMetrics,
            List<CurrencyStabilityRawMetrics>
                    allCurrencyStabilityRawMetrics,
            BigDecimal maximumWorldTotalTradeValue,
            BigDecimal minimumWorldCagr,
            BigDecimal maximumWorldCagr,
            BigDecimal maximumTurkeyExportPerformanceMetric,
            BigDecimal maximumTurkeyMarketShare
    ) {
        BigDecimal importMarketSizeScore =
                normalizeScore(
                        candidate.worldMetrics()
                                .totalTradeValueUsd(),
                        maximumWorldTotalTradeValue
                );

        BigDecimal importGrowthScore =
                normalizeMinMaxScore(
                        candidate.worldMetrics().cagrPercent(),
                        minimumWorldCagr,
                        maximumWorldCagr
                );

        BigDecimal turkeyExportPerformanceMetric =
                getTurkeyExportPerformanceMetric(
                        candidate.turkeyMetrics()
                );

        BigDecimal turkeyExportPerformanceScore =
                turkeyExportPerformanceMetric == null
                        ? null
                        : normalizeScore(
                                turkeyExportPerformanceMetric,
                                maximumTurkeyExportPerformanceMetric
                        );

        BigDecimal lowTurkeyMarketShareScore =
                calculateLowTurkeyMarketShareScore(
                        candidate.turkeyMarketSharePercent(),
                        maximumTurkeyMarketShare
                );

        BigDecimal marketShareOpportunityScore =
                calculateMarketShareOpportunityScore(
                        importMarketSizeScore,
                        importGrowthScore,
                        lowTurkeyMarketShareScore,
                        turkeyExportPerformanceScore
                );

        BigDecimal competitiveAccessibilityScore =
                candidate.competitiveAccessibilityMetrics()
                        .competitiveAccessibilityScore();

        MacroeconomicMetrics macroeconomicMetrics =
                macroeconomicStabilityCalculator.calculate(
                        candidate.macroeconomicRawMetrics(),
                        allMacroeconomicRawMetrics
                );

        BigDecimal macroeconomicStabilityScore =
                macroeconomicMetrics
                        .macroeconomicStabilityScore();

        CurrencyStabilityMetrics currencyStabilityMetrics =
                currencyStabilityCalculator.calculate(
                        candidate.currencyStabilityRawMetrics(),
                        allCurrencyStabilityRawMetrics
                );

        BigDecimal currencyStabilityScore =
                currencyStabilityMetrics
                        .currencyStabilityScore();

        LogisticsRawData logisticsRawData =
                logisticsDataProvider.getByCountryIso2Code(
                        candidate.country().getIso2Code()
                );

        LogisticsSuitabilityMetrics logisticsSuitabilityMetrics =
                logisticsSuitabilityCalculator.calculate(
                        logisticsRawData.approximateDistanceKm(),
                        logisticsRawData.averageTransitDays(),
                        logisticsRawData.transportMode(),
                        logisticsRawData.portConnectivityIndex()
                );

        BigDecimal logisticsSuitabilityScore =
                logisticsSuitabilityMetrics
                        .logisticsSuitabilityScore();

        TariffRawData tariffRawData =
                tariffDataProvider.getByCountryIso2Code(
                        candidate.country().getIso2Code()
                );

        TariffSuitabilityMetrics tariffSuitabilityMetrics =
                tariffSuitabilityCalculator.calculate(
                        tariffRawData.appliedTariffRatePercent(),
                        tariffRawData.preferentialTradeAgreement(),
                        tariffRawData.nonTariffBarrierIndex()
                );

        BigDecimal tariffSuitabilityScore =
                tariffSuitabilityMetrics
                        .tariffSuitabilityScore();

        BigDecimal overallScore =
                calculateOverallScore(
                        importMarketSizeScore,
                        importGrowthScore,
                        turkeyExportPerformanceScore,
                        marketShareOpportunityScore,
                        competitiveAccessibilityScore,
                        macroeconomicStabilityScore,
                        currencyStabilityScore,
                        logisticsSuitabilityScore,
                        tariffSuitabilityScore
                );

        return new ScoredCandidate(
                candidate.country(),
                candidate.worldMetrics(),
                candidate.turkeyMetrics(),
                candidate.turkeyMarketSharePercent(),
                candidate.competitiveAccessibilityMetrics(),
                importMarketSizeScore,
                importGrowthScore,
                turkeyExportPerformanceScore,
                marketShareOpportunityScore,
                competitiveAccessibilityScore,
                macroeconomicStabilityScore,
                currencyStabilityScore,
                logisticsSuitabilityMetrics,
                tariffSuitabilityMetrics,
                macroeconomicMetrics.dataCompleteness(),
                overallScore
        );
    }

    private BigDecimal calculateOverallScore(
            BigDecimal importMarketSizeScore,
            BigDecimal importGrowthScore,
            BigDecimal turkeyExportPerformanceScore,
            BigDecimal marketShareOpportunityScore,
            BigDecimal competitiveAccessibilityScore,
            BigDecimal macroeconomicStabilityScore,
            BigDecimal currencyStabilityScore,
            BigDecimal logisticsSuitabilityScore,
            BigDecimal tariffSuitabilityScore
    ) {
        BigDecimal weightedScoreSum =
                BigDecimal.ZERO;

        BigDecimal availableWeightSum =
                BigDecimal.ZERO;

        if (importMarketSizeScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            importMarketSizeScore.multiply(
                                    IMPORT_MARKET_SIZE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            IMPORT_MARKET_SIZE_WEIGHT
                    );
        }

        if (importGrowthScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            importGrowthScore.multiply(
                                    IMPORT_GROWTH_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            IMPORT_GROWTH_WEIGHT
                    );
        }

        if (turkeyExportPerformanceScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            turkeyExportPerformanceScore.multiply(
                                    TURKEY_EXPORT_PERFORMANCE_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TURKEY_EXPORT_PERFORMANCE_WEIGHT
                    );
        }

        if (marketShareOpportunityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            marketShareOpportunityScore.multiply(
                                    MARKET_SHARE_OPPORTUNITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            MARKET_SHARE_OPPORTUNITY_WEIGHT
                    );
        }

        if (competitiveAccessibilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            competitiveAccessibilityScore.multiply(
                                    COMPETITIVE_ACCESSIBILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            COMPETITIVE_ACCESSIBILITY_WEIGHT
                    );
        }

        if (macroeconomicStabilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            macroeconomicStabilityScore.multiply(
                                    MACROECONOMIC_STABILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            MACROECONOMIC_STABILITY_WEIGHT
                    );
        }

        if (currencyStabilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            currencyStabilityScore.multiply(
                                    CURRENCY_STABILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            CURRENCY_STABILITY_WEIGHT
                    );
        }

        if (logisticsSuitabilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            logisticsSuitabilityScore.multiply(
                                    LOGISTICS_SUITABILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            LOGISTICS_SUITABILITY_WEIGHT
                    );
        }

        if (tariffSuitabilityScore != null) {
            weightedScoreSum =
                    weightedScoreSum.add(
                            tariffSuitabilityScore.multiply(
                                    TARIFF_SUITABILITY_WEIGHT
                            )
                    );

            availableWeightSum =
                    availableWeightSum.add(
                            TARIFF_SUITABILITY_WEIGHT
                    );
        }

        if (availableWeightSum.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        /*
         * Eksik bir skor varsa, mevcut ağırlıklar kendi aralarında
         * yeniden normalize edilir. Eksik skor sessizce sıfır sayılmaz.
         */
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

    private BigDecimal calculateLowTurkeyMarketShareScore(
            BigDecimal turkeyMarketSharePercent,
            BigDecimal maximumTurkeyMarketShare
    ) {
        if (turkeyMarketSharePercent == null) {
            return null;
        }

        if (
                maximumTurkeyMarketShare == null
                        || maximumTurkeyMarketShare.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            return MAX_SCORE;
        }

        BigDecimal normalizedTurkeyShare =
                normalizeScore(
                        turkeyMarketSharePercent,
                        maximumTurkeyMarketShare
                );

        return MAX_SCORE
                .subtract(normalizedTurkeyShare)
                .max(BigDecimal.ZERO)
                .min(MAX_SCORE)
                .setScale(
                        SCORE_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateMarketShareOpportunityScore(
            BigDecimal importMarketSizeScore,
            BigDecimal importGrowthScore,
            BigDecimal lowTurkeyMarketShareScore,
            BigDecimal turkeyExportPerformanceScore
    ) {
        BigDecimal weightedScoreSum = BigDecimal.ZERO;
        BigDecimal availableWeightSum = BigDecimal.ZERO;

        BigDecimal marketSizeWeight = new BigDecimal("0.30");
        BigDecimal growthWeight = new BigDecimal("0.30");
        BigDecimal lowShareWeight = new BigDecimal("0.25");
        BigDecimal turkeyPerformanceWeight = new BigDecimal("0.15");

        if (importMarketSizeScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    importMarketSizeScore.multiply(marketSizeWeight)
            );
            availableWeightSum = availableWeightSum.add(marketSizeWeight);
        }

        if (importGrowthScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    importGrowthScore.multiply(growthWeight)
            );
            availableWeightSum = availableWeightSum.add(growthWeight);
        }

        if (lowTurkeyMarketShareScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    lowTurkeyMarketShareScore.multiply(lowShareWeight)
            );
            availableWeightSum = availableWeightSum.add(lowShareWeight);
        }

        if (turkeyExportPerformanceScore != null) {
            weightedScoreSum = weightedScoreSum.add(
                    turkeyExportPerformanceScore.multiply(
                            turkeyPerformanceWeight
                    )
            );
            availableWeightSum = availableWeightSum.add(
                    turkeyPerformanceWeight
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

    private BigDecimal getMaximumValue(
            List<CountryCandidate> candidates,
            Function<CountryCandidate, BigDecimal>
                    valueExtractor
    ) {
        return candidates.stream()
                .map(valueExtractor)
                .filter(value ->
                        value != null
                )
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal normalizeScore(
            BigDecimal value,
            BigDecimal maximumValue
    ) {
        if (
                value == null
                        || maximumValue == null
                        || maximumValue.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return value
                .divide(
                        maximumValue,
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

    private BigDecimal normalizeMinMaxScore(
            BigDecimal value,
            BigDecimal minimumValue,
            BigDecimal maximumValue
    ) {
        if (
                value == null
                        || minimumValue == null
                        || maximumValue == null
        ) {
            return null;
        }

        if (maximumValue.compareTo(minimumValue) == 0) {
            return new BigDecimal("50.00");
        }

        return value
                .subtract(minimumValue)
                .divide(
                        maximumValue.subtract(minimumValue),
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

    private BigDecimal calculateDataCompleteness(
            Analysis analysis,
            TradeMetrics metrics
    ) {
        int expectedYearCount =
                analysis.getEndYear()
                        - analysis.getStartYear()
                        + 1;

        if (expectedYearCount <= 0) {
            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal.valueOf(
                        metrics.availableYearCount()
                )
                .divide(
                        BigDecimal.valueOf(
                                expectedYearCount
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

    private record CountryCandidate(

            Country country,

            TradeMetrics worldMetrics,

            TradeMetrics turkeyMetrics,

            BigDecimal turkeyMarketSharePercent,

            CompetitiveAccessibilityMetrics
                    competitiveAccessibilityMetrics,

            MacroeconomicRawMetrics macroeconomicRawMetrics,

            CurrencyStabilityRawMetrics
                    currencyStabilityRawMetrics

    ) {
    }

    private record ScoredCandidate(

            Country country,

            TradeMetrics worldMetrics,

            TradeMetrics turkeyMetrics,

            BigDecimal turkeyMarketSharePercent,

            CompetitiveAccessibilityMetrics
                    competitiveAccessibilityMetrics,

            BigDecimal importMarketSizeScore,

            BigDecimal importGrowthScore,

            BigDecimal turkeyExportPerformanceScore,

            BigDecimal marketShareOpportunityScore,

            BigDecimal competitiveAccessibilityScore,

            BigDecimal macroeconomicStabilityScore,

            BigDecimal currencyStabilityScore,

            LogisticsSuitabilityMetrics logisticsSuitabilityMetrics,

            TariffSuitabilityMetrics tariffSuitabilityMetrics,

            BigDecimal economicDataCompleteness,

            BigDecimal overallScore

    ) {
    }
}