package com.exportpilot.economicindicator.dto;

import java.util.List;

public record WorldBankIndicatorResult(

        WorldBankResponseMetadata metadata,

        List<WorldBankIndicatorValueResponse> values
) {
}