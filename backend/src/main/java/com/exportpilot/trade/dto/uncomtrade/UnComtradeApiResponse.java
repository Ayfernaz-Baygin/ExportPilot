package com.exportpilot.trade.dto.uncomtrade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UnComtradeApiResponse(
        String elapsedTime,
        Integer count,
        List<UnComtradeDataRow> data
) {

    public List<UnComtradeDataRow> safeData() {
        return data == null ? List.of() : data;
    }
}