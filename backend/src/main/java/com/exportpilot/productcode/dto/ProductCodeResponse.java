package com.exportpilot.productcode.dto;

import com.exportpilot.productcode.entity.ProductCodeType;

import java.time.OffsetDateTime;

public record ProductCodeResponse(
        Long id,
        String code,
        ProductCodeType codeType,
        String description,
        Short classificationLevel,
        Long productId,
        String productName,
        Boolean active,
        OffsetDateTime createdAt
) {
}