package com.exportpilot.trade.provider;

import com.exportpilot.country.entity.Country;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.trade.entity.TradeDataStatus;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradePartnerScope;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.entity.TradeRevisionStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SampleTradeDataProvider
        implements TradeDataProvider {

    private static final String SOURCE =
            "SAMPLE_GENERATED";

    @Override
    public TradeDataSourceType getType() {
        return TradeDataSourceType.SAMPLE;
    }

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    /**
     * Synthetic development data generator.
     *
     * Generated records are not official trade statistics.
     */
    @Override
    public List<TradeRecord> generateRecords(
            ProductCode productCode,
            Integer startYear,
            Integer endYear,
            Country germany,
            Country poland,
            Country romania,
            Country turkey
    ) {
        validateParameters(
                productCode,
                startYear,
                endYear,
                germany,
                poland,
                romania,
                turkey
        );

        List<TradeRecord> records = new ArrayList<>();

        List<CountryProfile> profiles = List.of(
                new CountryProfile(
                        germany,
                        new BigDecimal("210000000"),
                        new BigDecimal("0.095"),
                        new BigDecimal("0.38")
                ),
                new CountryProfile(
                        poland,
                        new BigDecimal("105000000"),
                        new BigDecimal("0.145"),
                        new BigDecimal("0.39")
                ),
                new CountryProfile(
                        romania,
                        new BigDecimal("62000000"),
                        new BigDecimal("0.190"),
                        new BigDecimal("0.42")
                )
        );

        BigDecimal productMultiplier =
                calculateProductMultiplier(
                        productCode.getCode()
                );

        for (CountryProfile profile : profiles) {
            createHistoricalRecords(
                    records,
                    productCode,
                    profile,
                    turkey,
                    startYear,
                    endYear,
                    productMultiplier
            );
        }

        createCompetitorRecords(
                records,
                productCode,
                germany,
                poland,
                romania,
                turkey,
                profiles,
                startYear,
                endYear,
                productMultiplier
        );

        return records;
    }

    private void createHistoricalRecords(
            List<TradeRecord> records,
            ProductCode productCode,
            CountryProfile profile,
            Country turkey,
            Integer startYear,
            Integer endYear,
            BigDecimal productMultiplier
    ) {
        int yearIndex = 0;

        for (int year = startYear;
             year <= endYear;
             year++) {

            BigDecimal growthFactor =
                    BigDecimal.ONE
                            .add(
                                    profile.annualGrowthRate()
                            )
                            .pow(yearIndex);

            BigDecimal worldTotal =
                    profile.baseMarketValue()
                            .multiply(productMultiplier)
                            .multiply(growthFactor)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal turkeyValue =
                    worldTotal
                            .multiply(
                                    profile.turkeyMarketShare()
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            boolean estimated =
                    year == endYear;

            records.add(
                    createWorldTotalRecord(
                            profile.reporterCountry(),
                            productCode,
                            year,
                            worldTotal,
                            estimated
                    )
            );

            records.add(
                    createSpecificCountryRecord(
                            profile.reporterCountry(),
                            turkey,
                            productCode,
                            year,
                            turkeyValue,
                            estimated,
                            "TR"
                    )
            );

            yearIndex++;
        }
    }

    private void createCompetitorRecords(
            List<TradeRecord> records,
            ProductCode productCode,
            Country germany,
            Country poland,
            Country romania,
            Country turkey,
            List<CountryProfile> profiles,
            Integer startYear,
            Integer endYear,
            BigDecimal productMultiplier
    ) {
        for (CountryProfile profile : profiles) {
            BigDecimal worldTotal =
                    calculateValueForYear(
                            profile,
                            startYear,
                            endYear,
                            productMultiplier
                    );

            BigDecimal turkeyValue =
                    worldTotal
                            .multiply(
                                    profile.turkeyMarketShare()
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal remainingMarket =
                    worldTotal
                            .subtract(turkeyValue)
                            .max(BigDecimal.ZERO);

            BigDecimal firstCompetitorValue =
                    remainingMarket
                            .multiply(
                                    new BigDecimal("0.58")
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal secondCompetitorValue =
                    remainingMarket
                            .subtract(
                                    firstCompetitorValue
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            List<Country> competitors =
                    getCompetitors(
                            profile.reporterCountry(),
                            germany,
                            poland,
                            romania,
                            turkey
                    );

            records.add(
                    createSpecificCountryRecord(
                            profile.reporterCountry(),
                            competitors.get(0),
                            productCode,
                            endYear,
                            firstCompetitorValue,
                            true,
                            competitors.get(0)
                                    .getIso2Code()
                    )
            );

            records.add(
                    createSpecificCountryRecord(
                            profile.reporterCountry(),
                            competitors.get(1),
                            productCode,
                            endYear,
                            secondCompetitorValue,
                            true,
                            competitors.get(1)
                                    .getIso2Code()
                    )
            );
        }
    }

    private BigDecimal calculateValueForYear(
            CountryProfile profile,
            Integer startYear,
            Integer targetYear,
            BigDecimal productMultiplier
    ) {
        int yearDifference =
                targetYear - startYear;

        return profile.baseMarketValue()
                .multiply(productMultiplier)
                .multiply(
                        BigDecimal.ONE
                                .add(
                                        profile.annualGrowthRate()
                                )
                                .pow(yearDifference)
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private List<Country> getCompetitors(
            Country reporter,
            Country germany,
            Country poland,
            Country romania,
            Country turkey
    ) {
        return List.of(
                        germany,
                        poland,
                        romania
                )
                .stream()
                .filter(country ->
                        !country.getId()
                                .equals(reporter.getId())
                                && !country.getId()
                                .equals(turkey.getId())
                )
                .toList();
    }

    private TradeRecord createWorldTotalRecord(
            Country reporterCountry,
            ProductCode productCode,
            Integer year,
            BigDecimal tradeValueUsd,
            boolean estimated
    ) {
        return TradeRecord.builder()
                .source(SOURCE)
                .reporterCountry(reporterCountry)
                .partnerScope(
                        TradePartnerScope.WORLD_TOTAL
                )
                .partnerCountry(null)
                .productCode(productCode)
                .tradeFlow(TradeFlow.IMPORT)
                .tradeYear(year)
                .tradeValueUsd(tradeValueUsd)
                .sourceRecordId(
                        buildSourceRecordId(
                                "WORLD",
                                reporterCountry
                                        .getIso2Code(),
                                null,
                                productCode.getCode(),
                                year
                        )
                )
                .sourceRecordDate(
                        LocalDate.of(
                                year,
                                12,
                                31
                        )
                )
                .revisionStatus(
                        estimated
                                ? TradeRevisionStatus.ORIGINAL
                                : TradeRevisionStatus.FINAL
                )
                .dataStatus(
                        estimated
                                ? TradeDataStatus.ESTIMATED
                                : TradeDataStatus.AVAILABLE
                )
                .retrievedAt(
                        OffsetDateTime.now()
                )
                .build();
    }

    private TradeRecord createSpecificCountryRecord(
            Country reporterCountry,
            Country partnerCountry,
            ProductCode productCode,
            Integer year,
            BigDecimal tradeValueUsd,
            boolean estimated,
            String partnerIso2Code
    ) {
        BigDecimal unitValueUsdPerKg =
                calculateUnitValue(productCode);

        BigDecimal netWeightKg =
                tradeValueUsd.divide(
                        unitValueUsdPerKg,
                        3,
                        RoundingMode.HALF_UP
                );

        BigDecimal quantityTon =
                netWeightKg.divide(
                        new BigDecimal("1000"),
                        3,
                        RoundingMode.HALF_UP
                );

        return TradeRecord.builder()
                .source(SOURCE)
                .reporterCountry(reporterCountry)
                .partnerScope(
                        TradePartnerScope.SPECIFIC_COUNTRY
                )
                .partnerCountry(partnerCountry)
                .productCode(productCode)
                .tradeFlow(TradeFlow.IMPORT)
                .tradeYear(year)
                .tradeValueUsd(tradeValueUsd)
                .quantity(quantityTon)
                .quantityUnit("TON")
                .netWeightKg(netWeightKg)
                .sourceRecordId(
                        buildSourceRecordId(
                                null,
                                reporterCountry
                                        .getIso2Code(),
                                partnerIso2Code,
                                productCode.getCode(),
                                year
                        )
                )
                .sourceRecordDate(
                        LocalDate.of(
                                year,
                                12,
                                31
                        )
                )
                .revisionStatus(
                        estimated
                                ? TradeRevisionStatus.ORIGINAL
                                : TradeRevisionStatus.FINAL
                )
                .dataStatus(
                        estimated
                                ? TradeDataStatus.ESTIMATED
                                : TradeDataStatus.AVAILABLE
                )
                .retrievedAt(
                        OffsetDateTime.now()
                )
                .build();
    }

    private String buildSourceRecordId(
            String scope,
            String reporterIso2,
            String partnerIso2,
            String productCode,
            Integer year
    ) {
        if ("WORLD".equals(scope)) {
            return SOURCE
                    + "-WORLD-"
                    + reporterIso2.toUpperCase()
                    + "-"
                    + productCode
                    + "-"
                    + year;
        }

        return SOURCE
                + "-"
                + reporterIso2.toUpperCase()
                + "-"
                + partnerIso2.toUpperCase()
                + "-"
                + productCode
                + "-"
                + year;
    }

    private BigDecimal calculateProductMultiplier(
            String productCode
    ) {
        int hash =
                Math.abs(productCode.hashCode());

        int percentage =
                75 + (hash % 51);

        return new BigDecimal(percentage)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateUnitValue(
            ProductCode productCode
    ) {
        int hash =
                Math.abs(
                        productCode.getCode()
                                .hashCode()
                );

        BigDecimal adjustment =
                new BigDecimal(hash % 150)
                        .divide(
                                new BigDecimal("100"),
                                2,
                                RoundingMode.HALF_UP
                        );

        return new BigDecimal("3.50")
                .add(adjustment);
    }

    private void validateParameters(
            ProductCode productCode,
            Integer startYear,
            Integer endYear,
            Country germany,
            Country poland,
            Country romania,
            Country turkey
    ) {
        if (productCode == null) {
            throw new IllegalArgumentException(
                    "Product code is required."
            );
        }

        if (startYear == null
                || endYear == null) {
            throw new IllegalArgumentException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new IllegalArgumentException(
                    "Start year cannot be greater than end year."
            );
        }

        if (germany == null
                || poland == null
                || romania == null
                || turkey == null) {
            throw new IllegalArgumentException(
                    "Germany, Poland, Romania and Türkiye are required."
            );
        }
    }

    private record CountryProfile(
            Country reporterCountry,
            BigDecimal baseMarketValue,
            BigDecimal annualGrowthRate,
            BigDecimal turkeyMarketShare
    ) {
    }
}