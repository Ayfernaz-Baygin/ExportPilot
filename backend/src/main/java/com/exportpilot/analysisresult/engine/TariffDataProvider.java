package com.exportpilot.analysisresult.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class TariffDataProvider {

    private static final Map<String, TariffRawData>
            TARIFF_DATA_BY_ISO2 =
            Map.of(
                    "PL",
                    new TariffRawData(
                            new BigDecimal("0.00"),
                            true,
                            new BigDecimal("28.00"),
                            "EU-Türkiye Customs Union",
                            "Estimated reference data",
                            true
                    ),

                    "RO",
                    new TariffRawData(
                            new BigDecimal("0.00"),
                            true,
                            new BigDecimal("30.00"),
                            "EU-Türkiye Customs Union",
                            "Estimated reference data",
                            true
                    ),

                    "DE",
                    new TariffRawData(
                            new BigDecimal("0.00"),
                            true,
                            new BigDecimal("24.00"),
                            "EU-Türkiye Customs Union",
                            "Estimated reference data",
                            true
                    )
            );

    public TariffRawData getByCountryIso2Code(
            String countryIso2Code
    ) {
        if (countryIso2Code == null
                || countryIso2Code.isBlank()) {
            return emptyData();
        }

        return TARIFF_DATA_BY_ISO2.getOrDefault(
                countryIso2Code.trim().toUpperCase(),
                emptyData()
        );
    }

    private TariffRawData emptyData() {
        return new TariffRawData(
                null,
                null,
                null,
                null,
                null,
                true
        );
    }
}