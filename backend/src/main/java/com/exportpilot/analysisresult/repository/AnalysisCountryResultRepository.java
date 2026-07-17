package com.exportpilot.analysisresult.repository;

import com.exportpilot.analysisresult.dto.AnalysisCountryTarget;
import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalysisCountryResultRepository
        extends JpaRepository<AnalysisCountryResult, Long> {

    List<AnalysisCountryResult>
    findAllByAnalysisIdOrderByRankPositionAsc(
            Long analysisId
    );

    Optional<AnalysisCountryResult>
    findByAnalysisIdAndCountryId(
            Long analysisId,
            Long countryId
    );

    boolean existsByAnalysisIdAndCountryId(
            Long analysisId,
            Long countryId
    );

    void deleteAllByAnalysisId(Long analysisId);

    @Query("""
            select new com.exportpilot.analysisresult.dto.AnalysisCountryTarget(
                result.country.id,
                result.country.iso2Code,
                result.country.name
            )
            from AnalysisCountryResult result
            where result.analysis.id = :analysisId
            order by result.rankPosition asc
            """)
    List<AnalysisCountryTarget> findCountryTargetsByAnalysisId(
            @Param("analysisId") Long analysisId
    );
}