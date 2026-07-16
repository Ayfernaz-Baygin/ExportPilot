package com.exportpilot.analysisresult.repository;

import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import org.springframework.data.jpa.repository.JpaRepository;

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
}