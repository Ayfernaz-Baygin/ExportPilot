package com.exportpilot.trade.dto.uncomtrade;

public record UnComtradeImportResponse(
        String source,
        Long productCodeId,
        Long reporterCountryId,
        Long partnerCountryId,
        String partnerScope,
        String tradeFlow,
        Integer startYear,
        Integer endYear,
        Integer requestedYearCount,
        Integer receivedRowCount,
        Integer createdRecordCount,
        Integer skippedRecordCount
) {
}