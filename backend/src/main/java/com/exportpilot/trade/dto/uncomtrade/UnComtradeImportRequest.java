package com.exportpilot.trade.dto.uncomtrade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UnComtradeImportRequest(

        @NotNull
        Long productCodeId,

        @NotNull
        Long reporterCountryId,

        Long partnerCountryId,

        @NotNull
        String tradeFlow,

        @NotNull
        @Min(1900)
        @Max(2100)
        Integer startYear,

        @NotNull
        @Min(1900)
        @Max(2100)
        Integer endYear
) {
}