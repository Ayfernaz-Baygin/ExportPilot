package com.exportpilot.common.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}