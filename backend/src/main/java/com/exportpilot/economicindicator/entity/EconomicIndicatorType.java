package com.exportpilot.economicindicator.entity;

import lombok.Getter;

@Getter
public enum EconomicIndicatorType {

    GDP(
            "NY.GDP.MKTP.CD",
            "Gross Domestic Product",
            "CURRENT_USD"
    ),

    GDP_GROWTH(
            "NY.GDP.MKTP.KD.ZG",
            "GDP Growth",
            "ANNUAL_PERCENT"
    ),

    GDP_PER_CAPITA(
            "NY.GDP.PCAP.CD",
            "GDP Per Capita",
            "CURRENT_USD"
    ),

    INFLATION(
            "FP.CPI.TOTL.ZG",
            "Inflation",
            "ANNUAL_PERCENT"
    ),

    POPULATION(
            "SP.POP.TOTL",
            "Population",
            "PEOPLE"
    ),

    UNEMPLOYMENT(
            "SL.UEM.TOTL.ZS",
            "Unemployment",
            "PERCENT_OF_TOTAL_LABOR_FORCE"
    ),

    TRADE_GDP_RATIO(
            "NE.TRD.GNFS.ZS",
            "Trade as Percentage of GDP",
            "PERCENT_OF_GDP"
    );

    private final String worldBankCode;
    private final String displayName;
    private final String defaultUnit;

    EconomicIndicatorType(
            String worldBankCode,
            String displayName,
            String defaultUnit
    ) {
        this.worldBankCode = worldBankCode;
        this.displayName = displayName;
        this.defaultUnit = defaultUnit;
    }
}