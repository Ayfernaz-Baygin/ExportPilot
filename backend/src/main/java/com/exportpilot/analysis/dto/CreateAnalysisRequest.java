package com.exportpilot.analysis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAnalysisRequest(

        @NotNull(message = "Product ID is required.")
        Long productId,

        @NotNull(message = "Product code ID is required.")
        Long productCodeId,

        @NotNull(message = "Start year is required.")
        @Min(value = 2000, message = "Start year must be 2000 or later.")
        @Max(value = 2100, message = "Start year cannot exceed 2100.")
        Integer startYear,

        @NotNull(message = "End year is required.")
        @Min(value = 2000, message = "End year must be 2000 or later.")
        @Max(value = 2100, message = "End year cannot exceed 2100.")
        Integer endYear,

        @Size(
                max = 100,
                message = "Target region cannot exceed 100 characters."
        )
        String targetRegion,

        @Min(value = 1, message = "Maximum country count must be at least 1.")
        @Max(value = 100, message = "Maximum country count cannot exceed 100.")
        Integer maxCountries
) {
}