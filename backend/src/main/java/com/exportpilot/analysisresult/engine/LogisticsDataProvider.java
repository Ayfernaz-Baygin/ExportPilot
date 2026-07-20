package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class LogisticsDataProvider {

    private static final Map<String, LogisticsRawData>
            LOGISTICS_DATA_BY_ISO2 =
            Map.of(
                    "PL",
                    new LogisticsRawData(
                            new BigDecimal("2100"),
                            6,
                            "ROAD",
                            new BigDecimal("72.00"),
                            "Izmir",
                            "Warsaw",
                            "Estimated reference data",
                            true
                    ),

                    "RO",
                    new LogisticsRawData(
                            new BigDecimal("1250"),
                            4,
                            "ROAD",
                            new BigDecimal("68.00"),
                            "Izmir",
                            "Bucharest",
                            "Estimated reference data",
                            true
                    ),

                    "DE",
                    new LogisticsRawData(
                            new BigDecimal("2450"),
                            7,
                            "ROAD",
                            new BigDecimal("88.00"),
                            "Izmir",
                            "Berlin",
                            "Estimated reference data",
                            true
                    )
            );

    public LogisticsRawData getByCountryIso2Code(
            String countryIso2Code
    ) {
        if (countryIso2Code == null
                || countryIso2Code.isBlank()) {
            return emptyData();
        }

        return LOGISTICS_DATA_BY_ISO2.getOrDefault(
                countryIso2Code.trim().toUpperCase(),
                emptyData()
        );
    }

    private LogisticsRawData emptyData() {
        return new LogisticsRawData(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
    }
}
