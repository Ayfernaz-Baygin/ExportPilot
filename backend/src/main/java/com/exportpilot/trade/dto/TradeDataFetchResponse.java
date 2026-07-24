package com.exportpilot.trade.dto;

public record TradeDataFetchResponse(
        Long analysisId,
        Long productCodeId,
        String productCode,
        Integer startYear,
        Integer endYear,
        Integer createdRecordCount,
        Integer skippedRecordCount,
        String source,
        String message
) {
}