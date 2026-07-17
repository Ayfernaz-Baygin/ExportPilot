package com.exportpilot.economicindicator.service;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.analysisresult.dto.AnalysisCountryTarget;
import com.exportpilot.analysisresult.repository.AnalysisCountryResultRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.economicindicator.dto.AnalysisEconomicDataFetchResponse;
import com.exportpilot.economicindicator.dto.CountryEconomicFetchSummary;
import com.exportpilot.economicindicator.dto.EconomicIndicatorFetchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EconomicIndicatorBatchService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EconomicIndicatorBatchService.class
            );

    private final AnalysisRepository analysisRepository;

    private final AnalysisCountryResultRepository
            resultRepository;

    private final EconomicIndicatorService
            economicIndicatorService;

    public EconomicIndicatorBatchService(
            AnalysisRepository analysisRepository,
            AnalysisCountryResultRepository resultRepository,
            EconomicIndicatorService economicIndicatorService
    ) {
        this.analysisRepository = analysisRepository;
        this.resultRepository = resultRepository;
        this.economicIndicatorService =
                economicIndicatorService;
    }

    public AnalysisEconomicDataFetchResponse
    fetchIndicatorsForAnalysis(
            Long analysisId
    ) {
        Analysis analysis =
                analysisRepository.findById(analysisId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Analysis not found with id: "
                                                + analysisId
                                )
                        );

        List<AnalysisCountryTarget> countryTargets =
                resultRepository
                        .findCountryTargetsByAnalysisId(
                                analysisId
                        );

        if (countryTargets.isEmpty()) {
            throw new BusinessRuleException(
                    "No country results were found for analysis id: "
                            + analysisId
                            + ". Generate analysis results first."
            );
        }

        List<AnalysisCountryTarget> distinctTargets =
                getDistinctTargets(countryTargets);

        List<CountryEconomicFetchSummary> summaries =
                new ArrayList<>();

        for (AnalysisCountryTarget target
                : distinctTargets) {

            CountryEconomicFetchSummary summary =
                    fetchCountrySafely(
                            target,
                            analysis.getStartYear(),
                            analysis.getEndYear()
                    );

            summaries.add(summary);
        }

        return createBatchResponse(
                analysis,
                summaries
        );
    }

    private List<AnalysisCountryTarget> getDistinctTargets(
            List<AnalysisCountryTarget> targets
    ) {
        Map<Long, AnalysisCountryTarget> targetsById =
                new LinkedHashMap<>();

        for (AnalysisCountryTarget target : targets) {
            if (target == null
                    || target.countryId() == null) {
                continue;
            }

            targetsById.putIfAbsent(
                    target.countryId(),
                    target
            );
        }

        if (targetsById.isEmpty()) {
            throw new BusinessRuleException(
                    "Analysis results do not contain "
                            + "any valid countries."
            );
        }

        return List.copyOf(
                targetsById.values()
        );
    }

    private CountryEconomicFetchSummary fetchCountrySafely(
            AnalysisCountryTarget target,
            Integer startYear,
            Integer endYear
    ) {
        try {
            EconomicIndicatorFetchResponse response =
                    economicIndicatorService
                            .fetchCountryIndicators(
                                    target.countryId(),
                                    startYear,
                                    endYear
                            );

            return new CountryEconomicFetchSummary(
                    target.countryId(),
                    target.iso2Code(),
                    target.countryName(),

                    true,

                    response.savedRecordCount(),
                    response.availableRecordCount(),
                    response.missingRecordCount(),
                    response.staleRecordCount(),

                    null
            );

        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Economic indicator fetch failed "
                            + "for country id: {}, "
                            + "ISO2 code: {}, "
                            + "analysis period: {}-{}",
                    target.countryId(),
                    target.iso2Code(),
                    startYear,
                    endYear,
                    exception
            );

            return new CountryEconomicFetchSummary(
                    target.countryId(),
                    target.iso2Code(),
                    target.countryName(),

                    false,

                    0,
                    0,
                    0,
                    0,

                    resolveErrorMessage(exception)
            );
        }
    }

    private AnalysisEconomicDataFetchResponse
    createBatchResponse(
            Analysis analysis,
            List<CountryEconomicFetchSummary> summaries
    ) {
        int successfulCountryCount =
                (int) summaries.stream()
                        .filter(
                                CountryEconomicFetchSummary
                                        ::successful
                        )
                        .count();

        int failedCountryCount =
                summaries.size()
                        - successfulCountryCount;

        int totalSavedRecordCount =
                summaries.stream()
                        .filter(
                                CountryEconomicFetchSummary
                                        ::successful
                        )
                        .mapToInt(summary ->
                                safeInteger(
                                        summary.savedRecordCount()
                                )
                        )
                        .sum();

        int totalAvailableRecordCount =
                summaries.stream()
                        .filter(
                                CountryEconomicFetchSummary
                                        ::successful
                        )
                        .mapToInt(summary ->
                                safeInteger(
                                        summary.availableRecordCount()
                                )
                        )
                        .sum();

        int totalMissingRecordCount =
                summaries.stream()
                        .filter(
                                CountryEconomicFetchSummary
                                        ::successful
                        )
                        .mapToInt(summary ->
                                safeInteger(
                                        summary.missingRecordCount()
                                )
                        )
                        .sum();

        int totalStaleRecordCount =
                summaries.stream()
                        .filter(
                                CountryEconomicFetchSummary
                                        ::successful
                        )
                        .mapToInt(summary ->
                                safeInteger(
                                        summary.staleRecordCount()
                                )
                        )
                        .sum();

        return new AnalysisEconomicDataFetchResponse(
                analysis.getId(),

                analysis.getStartYear(),
                analysis.getEndYear(),

                summaries.size(),
                successfulCountryCount,
                failedCountryCount,

                totalSavedRecordCount,
                totalAvailableRecordCount,
                totalMissingRecordCount,
                totalStaleRecordCount,

                OffsetDateTime.now(),

                List.copyOf(summaries)
        );
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String resolveErrorMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return "Economic indicator retrieval failed.";
        }

        return message;
    }
}