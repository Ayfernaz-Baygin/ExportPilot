package com.exportpilot.product.dto;

import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String name,
        String scientificName,
        String category,
        String subCategory,
        String sector,
        String unit,
        String description,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}