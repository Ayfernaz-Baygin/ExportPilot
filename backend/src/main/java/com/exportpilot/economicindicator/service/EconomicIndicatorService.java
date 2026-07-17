package com.exportpilot.economicindicator.service;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.country.repository.CountryRepository;
import com.exportpilot.economicindicator.client.WorldBankClient;
import com.exportpilot.economicindicator.config.WorldBankProperties;
import com.exportpilot.economicindicator.dto.EconomicIndicatorFetchResponse;
import com.exportpilot.economicindicator.dto.EconomicIndicatorResponse;
import com.exportpilot.economicindicator.dto.WorldBankIndicatorResult;
import com.exportpilot.economicindicator.dto.WorldBankIndicatorValueResponse;
import com.exportpilot.economicindicator.entity.EconomicDataStatus;
import com.exportpilot.economicindicator.entity.EconomicIndicator;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import com.exportpilot.economicindicator.repository.EconomicIndicatorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EconomicIndicatorService {

    private static final int STALE_AFTER_YEAR_COUNT = 2;

    private final EconomicIndicatorRepository indicatorRepository;
    private final CountryRepository countryRepository;
    private final WorldBankClient worldBankClient;
    private final WorldBankProperties worldBankProperties;

    public EconomicIndicatorService(
            EconomicIndicatorRepository indicatorRepository,
            CountryRepository countryRepository,
            WorldBankClient worldBankClient,
            WorldBankProperties worldBankProperties
    ) {
        this.indicatorRepository = indicatorRepository;
        this.countryRepository = countryRepository;
        this.worldBankClient = worldBankClient;
        this.worldBankProperties = worldBankProperties;
    }

    @Transactional
    public EconomicIndicatorFetchResponse fetchCountryIndicators(
            Long countryId,
            Integer startYear,
            Integer endYear
    ) {
        validateYearRange(startYear, endYear);

        Country country = countryRepository.findById(countryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with id: " + countryId
                        )
                );

        validateCountry(country);

        OffsetDateTime retrievalTime = OffsetDateTime.now();

        for (EconomicIndicatorType indicatorType
                : EconomicIndicatorType.values()) {

            WorldBankIndicatorResult result =
                    worldBankClient.fetchIndicator(
                            country.getIso2Code(),
                            indicatorType,
                            startYear,
                            endYear
                    );

            saveIndicatorResult(
                    country,
                    indicatorType,
                    result,
                    retrievalTime
            );

            refreshLatestRecord(
                    country.getId(),
                    indicatorType
            );
        }

        List<EconomicIndicator> finalIndicators =
                indicatorRepository
                        .findAllByCountryIdOrderByIndicatorTypeAscYearDesc(
                                countryId
                        )
                        .stream()
                        .filter(indicator ->
                                indicator.getYear() >= startYear
                                        && indicator.getYear() <= endYear
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                EconomicIndicator
                                                        ::getIndicatorType
                                        )
                                        .thenComparing(
                                                EconomicIndicator::getYear,
                                                Comparator.reverseOrder()
                                        )
                        )
                        .toList();

        return createFetchResponse(
                country,
                startYear,
                endYear,
                retrievalTime,
                finalIndicators
        );
    }

    @Transactional(readOnly = true)
    public List<EconomicIndicatorResponse> getCountryIndicators(
            Long countryId
    ) {
        ensureCountryExists(countryId);

        return indicatorRepository
                .findAllByCountryIdOrderByIndicatorTypeAscYearDesc(
                        countryId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EconomicIndicatorResponse> getCountryIndicatorHistory(
            Long countryId,
            EconomicIndicatorType indicatorType
    ) {
        ensureCountryExists(countryId);

        if (indicatorType == null) {
            throw new BusinessRuleException(
                    "Economic indicator type is required."
            );
        }

        return indicatorRepository
                .findAllByCountryIdAndIndicatorTypeOrderByYearAsc(
                        countryId,
                        indicatorType
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EconomicIndicatorResponse getLatestCountryIndicator(
            Long countryId,
            EconomicIndicatorType indicatorType
    ) {
        ensureCountryExists(countryId);

        if (indicatorType == null) {
            throw new BusinessRuleException(
                    "Economic indicator type is required."
            );
        }

        EconomicIndicator indicator =
                indicatorRepository
                        .findByCountryIdAndIndicatorTypeAndLatestTrue(
                                countryId,
                                indicatorType
                        )
                        .orElseGet(() ->
                                indicatorRepository
                                        .findFirstByCountryIdAndIndicatorTypeAndValueIsNotNullOrderByYearDesc(
                                                countryId,
                                                indicatorType
                                        )
                                        .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                        "No available economic indicator "
                                                                + "was found for country id: "
                                                                + countryId
                                                                + " and indicator type: "
                                                                + indicatorType
                                                )
                                        )
                        );

        return toResponse(indicator);
    }

    private List<EconomicIndicator> saveIndicatorResult(
            Country country,
            EconomicIndicatorType indicatorType,
            WorldBankIndicatorResult result,
            OffsetDateTime retrievalTime
    ) {
        if (result == null
                || result.values() == null
                || result.values().isEmpty()) {
            return List.of();
        }

        OffsetDateTime sourceUpdatedAt =
                parseSourceUpdatedAt(
                        result.metadata() == null
                                ? null
                                : result.metadata().lastUpdated()
                );

        List<EconomicIndicator> savedIndicators =
                new ArrayList<>();

        for (WorldBankIndicatorValueResponse valueResponse
                : result.values()) {

            if (valueResponse.year() == null) {
                continue;
            }

            EconomicIndicator indicator =
                    indicatorRepository
                            .findByCountryIdAndIndicatorTypeAndYearAndSource(
                                    country.getId(),
                                    indicatorType,
                                    valueResponse.year(),
                                    worldBankProperties.sourceName()
                            )
                            .orElseGet(() ->
                                    EconomicIndicator.builder()
                                            .country(country)
                                            .indicatorType(indicatorType)
                                            .year(valueResponse.year())
                                            .source(
                                                    worldBankProperties
                                                            .sourceName()
                                            )
                                            .build()
                            );

            indicator.setValue(valueResponse.value());

            indicator.setUnit(
                    resolveUnit(
                            valueResponse.unit(),
                            indicatorType
                    )
            );

            indicator.setSourceIndicatorCode(
                    resolveSourceIndicatorCode(
                            valueResponse.indicatorCode(),
                            indicatorType
                    )
            );

            indicator.setDataStatus(
                    determineDataStatus(
                            valueResponse.value()
                    )
            );

            indicator.setSourceUpdatedAt(sourceUpdatedAt);
            indicator.setRetrievedAt(retrievalTime);
            indicator.setLatest(false);

            indicator.setTransformationVersion(
                    worldBankProperties.transformationVersion()
            );

            savedIndicators.add(
                    indicatorRepository.save(indicator)
            );
        }

        indicatorRepository.flush();

        return savedIndicators;
    }

    private void refreshLatestRecord(
            Long countryId,
            EconomicIndicatorType indicatorType
    ) {
        indicatorRepository.clearLatestFlag(
                countryId,
                indicatorType
        );

        indicatorRepository
                .findFirstByCountryIdAndIndicatorTypeAndValueIsNotNullOrderByYearDesc(
                        countryId,
                        indicatorType
                )
                .ifPresent(indicator -> {
                    indicator.setLatest(true);

                    if (isStaleLatestRecord(indicator.getYear())) {
                        indicator.setDataStatus(
                                EconomicDataStatus.STALE
                        );
                    } else {
                        indicator.setDataStatus(
                                EconomicDataStatus.AVAILABLE
                        );
                    }

                    indicatorRepository.save(indicator);
                    indicatorRepository.flush();
                });
    }

    private EconomicDataStatus determineDataStatus(
            BigDecimal value
    ) {
        if (value == null) {
            return EconomicDataStatus.MISSING;
        }

        return EconomicDataStatus.AVAILABLE;
    }

    private boolean isStaleLatestRecord(Integer year) {
        if (year == null) {
            return false;
        }

        int currentYear = LocalDate.now().getYear();

        return currentYear - year > STALE_AFTER_YEAR_COUNT;
    }

    private String resolveUnit(
            String worldBankUnit,
            EconomicIndicatorType indicatorType
    ) {
        if (worldBankUnit == null
                || worldBankUnit.isBlank()) {
            return indicatorType.getDefaultUnit();
        }

        return worldBankUnit.trim();
    }

    private String resolveSourceIndicatorCode(
            String responseIndicatorCode,
            EconomicIndicatorType indicatorType
    ) {
        if (responseIndicatorCode == null
                || responseIndicatorCode.isBlank()) {
            return indicatorType.getWorldBankCode();
        }

        return responseIndicatorCode.trim();
    }

    private OffsetDateTime parseSourceUpdatedAt(
            String sourceUpdatedDate
    ) {
        if (sourceUpdatedDate == null
                || sourceUpdatedDate.isBlank()) {
            return null;
        }

        try {
            return LocalDate
                    .parse(sourceUpdatedDate)
                    .atStartOfDay()
                    .atOffset(ZoneOffset.UTC);

        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private EconomicIndicatorFetchResponse createFetchResponse(
            Country country,
            Integer startYear,
            Integer endYear,
            OffsetDateTime retrievalTime,
            List<EconomicIndicator> indicators
    ) {
        int availableCount = (int) indicators.stream()
                .filter(indicator ->
                        indicator.getDataStatus()
                                == EconomicDataStatus.AVAILABLE
                )
                .count();

        int missingCount = (int) indicators.stream()
                .filter(indicator ->
                        indicator.getDataStatus()
                                == EconomicDataStatus.MISSING
                )
                .count();

        int staleCount = (int) indicators.stream()
                .filter(indicator ->
                        indicator.getDataStatus()
                                == EconomicDataStatus.STALE
                )
                .count();

        return new EconomicIndicatorFetchResponse(
                country.getId(),
                country.getIso2Code(),
                country.getName(),
                startYear,
                endYear,
                EconomicIndicatorType.values().length,
                indicators.size(),
                availableCount,
                missingCount,
                staleCount,
                retrievalTime,
                indicators.stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private EconomicIndicatorResponse toResponse(
            EconomicIndicator indicator
    ) {
        Country country = indicator.getCountry();

        return new EconomicIndicatorResponse(
                indicator.getId(),

                country.getId(),
                country.getIso2Code(),
                country.getIso3Code(),
                country.getName(),

                indicator.getIndicatorType(),
                indicator.getIndicatorType().getDisplayName(),
                indicator.getSourceIndicatorCode(),

                indicator.getYear(),
                indicator.getValue(),
                indicator.getUnit(),

                indicator.getDataStatus(),
                indicator.getLatest(),

                indicator.getSource(),
                indicator.getSourceUpdatedAt(),
                indicator.getRetrievedAt(),
                indicator.getTransformationVersion()
        );
    }

    private void validateYearRange(
            Integer startYear,
            Integer endYear
    ) {
        if (startYear == null || endYear == null) {
            throw new BusinessRuleException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }

        if (startYear < 1900 || endYear > 2100) {
            throw new BusinessRuleException(
                    "Economic indicator year range must be "
                            + "between 1900 and 2100."
            );
        }
    }

    private void validateCountry(Country country) {
        if (country.getIso2Code() == null
                || country.getIso2Code().isBlank()) {
            throw new BusinessRuleException(
                    "Country does not have a valid ISO2 code."
            );
        }

        if (!Boolean.TRUE.equals(country.getActive())) {
            throw new BusinessRuleException(
                    "Economic indicators cannot be fetched "
                            + "for an inactive country."
            );
        }
    }

    private void ensureCountryExists(Long countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException(
                    "Country not found with id: " + countryId
            );
        }
    }
}