package com.exportpilot.country.dto;

import java.time.OffsetDateTime;

public record CountryResponse(
        Long id,
        String iso2Code,
        String iso3Code,
        String name,
        String region,
        String incomeGroup,
        Boolean active,
        OffsetDateTime createdAt
) {
}