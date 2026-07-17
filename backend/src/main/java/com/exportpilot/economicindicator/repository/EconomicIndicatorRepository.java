package com.exportpilot.economicindicator.repository;

import com.exportpilot.economicindicator.entity.EconomicIndicator;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EconomicIndicatorRepository
        extends JpaRepository<EconomicIndicator, Long> {

    List<EconomicIndicator>
    findAllByCountryIdOrderByIndicatorTypeAscYearDesc(
            Long countryId
    );

    List<EconomicIndicator>
    findAllByCountryIdAndIndicatorTypeOrderByYearAsc(
            Long countryId,
            EconomicIndicatorType indicatorType
    );

    List<EconomicIndicator>
    findAllByCountryIdAndIndicatorTypeAndYearBetweenOrderByYearAsc(
            Long countryId,
            EconomicIndicatorType indicatorType,
            Integer startYear,
            Integer endYear
    );

    List<EconomicIndicator>
    findAllByCountryIdInAndYearBetweenOrderByCountryIdAscIndicatorTypeAscYearAsc(
            List<Long> countryIds,
            Integer startYear,
            Integer endYear
    );

    Optional<EconomicIndicator>
    findByCountryIdAndIndicatorTypeAndYearAndSource(
            Long countryId,
            EconomicIndicatorType indicatorType,
            Integer year,
            String source
    );

    Optional<EconomicIndicator>
    findFirstByCountryIdAndIndicatorTypeAndValueIsNotNullOrderByYearDesc(
            Long countryId,
            EconomicIndicatorType indicatorType
    );

    Optional<EconomicIndicator>
    findByCountryIdAndIndicatorTypeAndLatestTrue(
            Long countryId,
            EconomicIndicatorType indicatorType
    );

    boolean existsByCountryIdAndIndicatorTypeAndYearAndSource(
            Long countryId,
            EconomicIndicatorType indicatorType,
            Integer year,
            String source
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update EconomicIndicator indicator
               set indicator.latest = false
             where indicator.country.id = :countryId
               and indicator.indicatorType = :indicatorType
            """)
    int clearLatestFlag(
            @Param("countryId")
            Long countryId,

            @Param("indicatorType")
            EconomicIndicatorType indicatorType
    );
}