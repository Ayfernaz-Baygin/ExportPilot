package com.exportpilot.analysisresult.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiMarketReportRepository
        extends JpaRepository<AiMarketReportEntity, Long> {

    Optional<AiMarketReportEntity> findByAnalysisId(Long analysisId);
}