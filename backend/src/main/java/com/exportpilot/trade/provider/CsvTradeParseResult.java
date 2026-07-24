package com.exportpilot.trade.provider;

import com.exportpilot.trade.entity.TradeRecord;

import java.util.List;

public record CsvTradeParseResult(
        List<TradeRecord> records,
        int invalidRecordCount,
        List<String> errors
) {
}