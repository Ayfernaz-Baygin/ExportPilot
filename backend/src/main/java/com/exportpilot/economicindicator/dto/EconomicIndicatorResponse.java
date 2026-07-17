package com.exportpilot.economicindicator.dto;

import com.exportpilot.economicindicator.entity.EconomicDataStatus;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EconomicIndicatorResponse(

        Long id,

        Long countryId,
        String countryIso2Code,
        String countryIso3Code,
        String countryName,

        EconomicIndicatorType indicatorType,
        String indicatorName,
        String sourceIndicatorCode,

        Integer year,
        BigDecimal value,
        String unit,

        EconomicDataStatus dataStatus,
        Boolean latest,

        String source,
        OffsetDateTime sourceUpdatedAt,
        OffsetDateTime retrievedAt,
        String transformationVersion
) {
}