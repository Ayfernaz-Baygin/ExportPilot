package com.exportpilot.analysis.dto;

import com.exportpilot.analysis.entity.AnalysisStatus;
import com.exportpilot.productcode.entity.ProductCodeType;

import java.time.OffsetDateTime;

public record AnalysisResponse(
        Long id,

        Long productId,
        String productName,

        Long productCodeId,
        String productCode,
        ProductCodeType productCodeType,

        Integer startYear,
        Integer endYear,
        String targetRegion,
        Integer maxCountries,

        AnalysisStatus status,
        String scoringModelVersion,

        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
}