package com.exportpilot.economicindicator.dto;

public record WorldBankResponseMetadata(

        Integer page,
        Integer pages,
        Integer perPage,
        Integer total,
        String lastUpdated
) {
}