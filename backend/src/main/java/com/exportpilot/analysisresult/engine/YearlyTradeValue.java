package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record YearlyTradeValue(
        Integer year,
        BigDecimal tradeValueUsd
) {
}