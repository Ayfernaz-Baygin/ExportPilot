package com.exportpilot.trade.dto;

import java.util.List;

public record TradeCsvImportResponse(
        Long analysisId,
        Long productCodeId,
        String productCode,
        Integer startYear,
        Integer endYear,
        String fileName,
        Integer createdRecordCount,
        Integer skippedRecordCount,
        Integer invalidRecordCount,
        String source,
        List<String> errors,
        String message
) {
}