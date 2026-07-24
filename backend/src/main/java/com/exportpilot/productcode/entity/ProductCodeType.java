package com.exportpilot.productcode.entity;

public enum ProductCodeType {

    HS,
    GTIP;

    public boolean supportsLength(int length) {
        return switch (this) {
            case HS -> length == 2
                    || length == 4
                    || length == 6;

            case GTIP -> length == 12;
        };
    }
}