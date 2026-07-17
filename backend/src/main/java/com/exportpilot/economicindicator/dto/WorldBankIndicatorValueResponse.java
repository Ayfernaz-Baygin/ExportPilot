package com.exportpilot.economicindicator.dto;

import java.math.BigDecimal;

public record WorldBankIndicatorValueResponse(

        String indicatorCode,
        String indicatorName,

        String countryIso2Code,
        String countryIso3Code,
        String countryName,

        Integer year,
        BigDecimal value,

        String unit,
        String observationStatus,
        Integer decimalPlaces
) {
}