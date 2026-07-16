package com.exportpilot.analysisresult.service;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.entity.AnalysisStatus;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.analysisresult.engine.TradeMetrics;
import com.exportpilot.analysisresult.engine.TradeMetricsCalculator;
import com.exportpilot.analysisresult.engine.YearlyTradeValue;
import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import com.exportpilot.analysisresult.repository.AnalysisCountryResultRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.trade.entity.TradeFlow;
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
import java.util.stream.Collectors;

@Service
public class AnalysisCountryResultGenerator {

    private static final BigDecimal TURKEY_EXPORT_PERFORMANCE_WEIGHT =
            new BigDecimal("0.70");

    private static final BigDecimal IMPORT_GROWTH_WEIGHT =
            new BigDecimal("0.30");

    private static final BigDecimal MAX_SCORE =
            new BigDecimal("100.00");

    private static final int SCORE_SCALE = 2;

    private final AnalysisRepository analysisRepository;
    private final AnalysisCountryResultRepository resultRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final TradeMetricsCalculator metricsCalculator;

    public AnalysisCountryResultGenerator(
            AnalysisRepository analysisRepository,
            AnalysisCountryResultRepository resultRepository,
            TradeRecordRepository tradeRecordRepository,
            TradeMetricsCalculator metricsCalculator
    ) {
        this.analysisRepository = analysisRepository;
        this.resultRepository = resultRepository;
        this.tradeRecordRepository = tradeRecordRepository;
        this.metricsCalculator = metricsCalculator;
    }

    @Transactional
    public List<AnalysisCountryResult> generateResults(Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis not found with id: " + analysisId
                        )
                );

        analysis.setStatus(AnalysisStatus.RUNNING);
        analysis.setCompletedAt(null);
        analysisRepository.save(analysis);

        try {
            resultRepository.deleteAllByAnalysisId(analysisId);
            resultRepository.flush();

            List<TradeRecord> tradeRecords =
                    getRelevantTradeRecords(analysis);

            if (tradeRecords.isEmpty()) {
                throw new BusinessRuleException(
                        "No trade records were found for analysis id: "
                                + analysisId
                );
            }

            Map<Country, List<TradeRecord>> recordsByCountry =
                    tradeRecords.stream()
                            .collect(Collectors.groupingBy(
                                    TradeRecord::getReporterCountry,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            List<CountryCandidate> candidates =
                    recordsByCountry.entrySet().stream()
                            .map(entry ->
                                    createCandidate(
                                            entry.getKey(),
                                            entry.getValue()
                                    )
                            )
                            .toList();

            List<AnalysisCountryResult> results =
                    createRankedResults(
                            analysis,
                            candidates
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

    private List<TradeRecord> getRelevantTradeRecords(
            Analysis analysis
    ) {
        List<TradeRecord> records =
                tradeRecordRepository
                        .findAllByProductCodeIdAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
                                analysis.getProductCode().getId(),
                                TradeFlow.IMPORT,
                                analysis.getStartYear(),
                                analysis.getEndYear()
                        );

        String targetRegion = analysis.getTargetRegion();

        if (targetRegion == null || targetRegion.isBlank()) {
            return records;
        }

        return records.stream()
                .filter(record ->
                        record.getReporterCountry().getRegion() != null
                )
                .filter(record ->
                        record.getReporterCountry()
                                .getRegion()
                                .equalsIgnoreCase(targetRegion.trim())
                )
                .toList();
    }

    private CountryCandidate createCandidate(
            Country country,
            List<TradeRecord> countryRecords
    ) {
        Map<Integer, BigDecimal> yearlyTotals =
                countryRecords.stream()
                        .filter(record ->
                                record.getTradeYear() != null
                        )
                        .filter(record ->
                                record.getTradeValueUsd() != null
                        )
                        .collect(Collectors.groupingBy(
                                TradeRecord::getTradeYear,
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        TradeRecord::getTradeValueUsd,
                                        BigDecimal::add
                                )
                        ));

        List<YearlyTradeValue> yearlyValues =
                yearlyTotals.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry ->
                                new YearlyTradeValue(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        TradeMetrics metrics =
                metricsCalculator.calculate(yearlyValues);

        return new CountryCandidate(
                country,
                metrics
        );
    }

    private List<AnalysisCountryResult> createRankedResults(
            Analysis analysis,
            List<CountryCandidate> candidates
    ) {
        BigDecimal maximumTurkeyExportValue =
                candidates.stream()
                        .map(candidate ->
                                candidate.metrics()
                                        .totalTradeValueUsd()
                        )
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal maximumPositiveCagr =
                candidates.stream()
                        .map(candidate ->
                                candidate.metrics().cagrPercent()
                        )
                        .filter(value -> value != null)
                        .filter(value ->
                                value.compareTo(BigDecimal.ZERO) > 0
                        )
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        List<ScoredCandidate> scoredCandidates =
                candidates.stream()
                        .map(candidate ->
                                scoreCandidate(
                                        candidate,
                                        maximumTurkeyExportValue,
                                        maximumPositiveCagr
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

        for (int index = 0;
             index < scoredCandidates.size();
             index++) {

            ScoredCandidate candidate =
                    scoredCandidates.get(index);

            TradeMetrics metrics = candidate.metrics();

            AnalysisCountryResult result =
                    AnalysisCountryResult.builder()
                            .analysis(analysis)
                            .country(candidate.country())
                            .rankPosition(index + 1)
                            .overallScore(
                                    candidate.overallScore()
                            )

                            // Şu an elimizde hedef ülkenin toplam dünya
                            // ithalatı bulunmadığı için bu skor boş kalır.
                            .importMarketSizeScore(null)

                            .importGrowthScore(
                                    candidate.growthScore()
                            )

                            // Mevcut SAMPLE kayıtları hedef ülkenin
                            // Türkiye'den yaptığı ithalatı temsil eder.
                            .turkeyExportPerformanceScore(
                                    candidate
                                            .turkeyExportPerformanceScore()
                            )

                            .marketShareOpportunityScore(null)
                            .competitiveAccessibilityScore(null)
                            .macroeconomicStabilityScore(null)
                            .currencyStabilityScore(null)
                            .logisticsSuitabilityScore(null)
                            .tariffSuitabilityScore(null)

                            .dataCompleteness(
                                    calculateDataCompleteness(
                                            analysis,
                                            metrics
                                    )
                            )
                            .firstYear(metrics.firstYear())
                            .lastYear(metrics.lastYear())
                            .availableYearCount(
                                    metrics.availableYearCount()
                            )
                            .firstYearTradeValueUsd(
                                    metrics.firstYearTradeValueUsd()
                            )
                            .lastYearTradeValueUsd(
                                    metrics.lastYearTradeValueUsd()
                            )
                            .totalTradeValueUsd(
                                    metrics.totalTradeValueUsd()
                            )
                            .averageTradeValueUsd(
                                    metrics.averageTradeValueUsd()
                            )
                            .absoluteGrowthUsd(
                                    metrics.absoluteGrowthUsd()
                            )
                            .growthRatePercent(
                                    metrics.growthRatePercent()
                            )
                            .cagrPercent(
                                    metrics.cagrPercent()
                            )
                            .calculatedAt(
                                    OffsetDateTime.now()
                            )
                            .build();

            results.add(result);
        }

        return results;
    }

    private ScoredCandidate scoreCandidate(
            CountryCandidate candidate,
            BigDecimal maximumTurkeyExportValue,
            BigDecimal maximumPositiveCagr
    ) {
        BigDecimal turkeyExportPerformanceScore =
                normalizeScore(
                        candidate.metrics().totalTradeValueUsd(),
                        maximumTurkeyExportValue
                );

        BigDecimal positiveCagr =
                candidate.metrics().cagrPercent() == null
                        ? BigDecimal.ZERO
                        : candidate.metrics()
                                .cagrPercent()
                                .max(BigDecimal.ZERO);

        BigDecimal growthScore =
                normalizeScore(
                        positiveCagr,
                        maximumPositiveCagr
                );

        BigDecimal overallScore =
                turkeyExportPerformanceScore
                        .multiply(
                                TURKEY_EXPORT_PERFORMANCE_WEIGHT
                        )
                        .add(
                                growthScore.multiply(
                                        IMPORT_GROWTH_WEIGHT
                                )
                        )
                        .setScale(
                                SCORE_SCALE,
                                RoundingMode.HALF_UP
                        );

        return new ScoredCandidate(
                candidate.country(),
                candidate.metrics(),
                turkeyExportPerformanceScore,
                growthScore,
                overallScore
        );
    }

    private BigDecimal normalizeScore(
            BigDecimal value,
            BigDecimal maximumValue
    ) {
        if (value == null
                || maximumValue == null
                || maximumValue.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO.setScale(
                    SCORE_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        return value
                .divide(
                        maximumValue,
                        8,
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
                        BigDecimal.valueOf(expectedYearCount),
                        8,
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
            TradeMetrics metrics
    ) {
    }

    private record ScoredCandidate(
            Country country,
            TradeMetrics metrics,
            BigDecimal turkeyExportPerformanceScore,
            BigDecimal growthScore,
            BigDecimal overallScore
    ) {
    }
}