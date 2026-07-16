package com.exportpilot.trade.dto;

import com.exportpilot.productcode.entity.ProductCodeType;
import com.exportpilot.trade.entity.TradeDataStatus;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradeRevisionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TradeRecordResponse(
        Long id,
        String source,

        Long reporterCountryId,
        String reporterIso2Code,
        String reporterCountryName,

        Long partnerCountryId,
        String partnerIso2Code,
        String partnerCountryName,

        Long productCodeId,
        String productCode,
        ProductCodeType productCodeType,
        String productDescription,

        TradeFlow tradeFlow,
        Integer tradeYear,

        BigDecimal tradeValueUsd,
        BigDecimal quantity,
        String quantityUnit,
        BigDecimal netWeightKg,

        String sourceRecordId,
        LocalDate sourceRecordDate,

        TradeRevisionStatus revisionStatus,
        TradeDataStatus dataStatus,

        OffsetDateTime retrievedAt,
        OffsetDateTime createdAt
) {
}