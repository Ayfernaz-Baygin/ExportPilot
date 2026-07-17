package com.exportpilot.economicindicator.client;

public class WorldBankApiException extends RuntimeException {

    public WorldBankApiException(String message) {
        super(message);
    }

    public WorldBankApiException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}