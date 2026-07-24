package com.exportpilot.trade.provider;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.country.repository.CountryRepository;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.trade.entity.TradeDataStatus;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradePartnerScope;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.entity.TradeRevisionStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CsvTradeDataProvider
        implements TradeDataProvider {

    private static final String SOURCE = "CSV_IMPORT";

    private static final int MAX_REPORTED_ERRORS = 20;

    private final CountryRepository countryRepository;

    public CsvTradeDataProvider(
            CountryRepository countryRepository
    ) {
        this.countryRepository = countryRepository;
    }

    @Override
    public TradeDataSourceType getType() {
        return TradeDataSourceType.CSV;
    }

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    /*
     * Bu method TradeDataProvider'ın mevcut yapısı nedeniyle burada kalıyor.
     * CSV işlemi generateRecords yerine parseFile methoduyla yapılır.
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
        throw new UnsupportedOperationException(
                "CSV data must be imported using parseFile."
        );
    }

    public CsvTradeParseResult parseFile(
            MultipartFile file,
            ProductCode productCode,
            Integer analysisStartYear,
            Integer analysisEndYear
    ) {
        validateFile(file);

        if (productCode == null) {
            throw new BusinessRuleException(
                    "Analysis product code is required."
            );
        }

        List<TradeRecord> records = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int invalidRecordCount = 0;

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                CSVParser parser = csvFormat.parse(reader)
        ) {
            validateHeaders(parser);

            for (CSVRecord csvRecord : parser) {
                try {
                    TradeRecord tradeRecord = convertRecord(
                            csvRecord,
                            productCode,
                            analysisStartYear,
                            analysisEndYear
                    );

                    records.add(tradeRecord);
                } catch (RuntimeException exception) {
                    invalidRecordCount++;

                    if (errors.size() < MAX_REPORTED_ERRORS) {
                        errors.add(
                                "Row "
                                        + csvRecord.getRecordNumber()
                                        + ": "
                                        + exception.getMessage()
                        );
                    }
                }
            }
        } catch (IOException exception) {
            throw new BusinessRuleException(
                    "CSV file could not be read: "
                            + exception.getMessage()
            );
        }

        if (records.isEmpty()) {
            throw new BusinessRuleException(
                    "CSV file does not contain any valid trade records."
            );
        }

        return new CsvTradeParseResult(
                records,
                invalidRecordCount,
                errors
        );
    }

    private TradeRecord convertRecord(
            CSVRecord csvRecord,
            ProductCode productCode,
            Integer analysisStartYear,
            Integer analysisEndYear
    ) {
        String reporterIso2 = requiredValue(
                csvRecord,
                "reporterIso2"
        ).toUpperCase(Locale.ROOT);

        String partnerIso2 = optionalValue(
                csvRecord,
                "partnerIso2"
        );

        TradeFlow tradeFlow = parseTradeFlow(
                requiredValue(csvRecord, "tradeFlow")
        );

        Integer year = parseYear(
                requiredValue(csvRecord, "year")
        );

        validateYearRange(
                year,
                analysisStartYear,
                analysisEndYear
        );

        BigDecimal tradeValueUsd = parseRequiredDecimal(
                csvRecord,
                "tradeValueUsd"
        );

        BigDecimal quantity = parseOptionalDecimal(
                csvRecord,
                "quantity"
        );

        String quantityUnit = optionalValue(
                csvRecord,
                "quantityUnit"
        );

        BigDecimal netWeightKg = parseOptionalDecimal(
                csvRecord,
                "netWeightKg"
        );

        Country reporterCountry = getCountry(
                reporterIso2
        );

        boolean worldTotal =
                partnerIso2 == null
                        || partnerIso2.isBlank()
                        || "WORLD".equalsIgnoreCase(partnerIso2);

        Country partnerCountry = null;

        TradePartnerScope partnerScope;

        if (worldTotal) {
            partnerScope = TradePartnerScope.WORLD_TOTAL;
        } else {
            partnerIso2 =
                    partnerIso2.toUpperCase(Locale.ROOT);

            partnerCountry = getCountry(partnerIso2);

            partnerScope =
                    TradePartnerScope.SPECIFIC_COUNTRY;
        }

        return TradeRecord.builder()
                .source(SOURCE)
                .reporterCountry(reporterCountry)
                .partnerScope(partnerScope)
                .partnerCountry(partnerCountry)
                .productCode(productCode)
                .tradeFlow(tradeFlow)
                .tradeYear(year)
                .tradeValueUsd(tradeValueUsd)
                .quantity(quantity)
                .quantityUnit(quantityUnit)
                .netWeightKg(netWeightKg)
                .sourceRecordId(
                        buildSourceRecordId(
                                reporterIso2,
                                worldTotal
                                        ? "WORLD"
                                        : partnerIso2,
                                productCode.getCode(),
                                tradeFlow,
                                year
                        )
                )
                .sourceRecordDate(
                        LocalDate.of(year, 12, 31)
                )
                .revisionStatus(
                        TradeRevisionStatus.ORIGINAL
                )
                .dataStatus(
                        TradeDataStatus.AVAILABLE
                )
                .retrievedAt(
                        OffsetDateTime.now()
                )
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(
                    "CSV file is required."
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null
                || !originalFilename
                .toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {
            throw new BusinessRuleException(
                    "Only CSV files are supported."
            );
        }
    }

    private void validateHeaders(CSVParser parser) {
        List<String> requiredHeaders = List.of(
                "reporterIso2",
                "partnerIso2",
                "tradeFlow",
                "year",
                "tradeValueUsd",
                "quantity",
                "quantityUnit",
                "netWeightKg"
        );

        List<String> actualHeaders =
                parser.getHeaderNames();

        for (String requiredHeader : requiredHeaders) {
            boolean exists = actualHeaders.stream()
                    .anyMatch(header ->
                            header.equalsIgnoreCase(
                                    requiredHeader
                            )
                    );

            if (!exists) {
                throw new BusinessRuleException(
                        "Required CSV column is missing: "
                                + requiredHeader
                );
            }
        }
    }

    private Country getCountry(String iso2Code) {
        return countryRepository
                .findByIso2CodeIgnoreCase(iso2Code)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with ISO2 code: "
                                        + iso2Code
                        )
                );
    }

    private String requiredValue(
            CSVRecord csvRecord,
            String header
    ) {
        String value = optionalValue(
                csvRecord,
                header
        );

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    header + " is required."
            );
        }

        return value;
    }

    private String optionalValue(
            CSVRecord csvRecord,
            String header
    ) {
        String value = csvRecord.get(header);

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Integer parseYear(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid year: " + value
            );
        }
    }

    private TradeFlow parseTradeFlow(String value) {
        try {
            return TradeFlow.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid tradeFlow: "
                            + value
                            + ". Expected IMPORT or EXPORT."
            );
        }
    }

    private BigDecimal parseRequiredDecimal(
            CSVRecord csvRecord,
            String header
    ) {
        String value = requiredValue(
                csvRecord,
                header
        );

        return parseDecimal(value, header);
    }

    private BigDecimal parseOptionalDecimal(
            CSVRecord csvRecord,
            String header
    ) {
        String value = optionalValue(
                csvRecord,
                header
        );

        if (value == null) {
            return null;
        }

        return parseDecimal(value, header);
    }

    private BigDecimal parseDecimal(
            String value,
            String fieldName
    ) {
        try {
            BigDecimal parsedValue =
                    new BigDecimal(value);

            if (parsedValue.signum() < 0) {
                throw new IllegalArgumentException(
                        fieldName
                                + " cannot be negative."
                );
            }

            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid numeric value for "
                            + fieldName
                            + ": "
                            + value
            );
        }
    }

    private void validateYearRange(
            Integer year,
            Integer startYear,
            Integer endYear
    ) {
        if (year < startYear || year > endYear) {
            throw new IllegalArgumentException(
                    "Year "
                            + year
                            + " is outside analysis range "
                            + startYear
                            + "-"
                            + endYear
                            + "."
            );
        }
    }

    private String buildSourceRecordId(
            String reporterIso2,
            String partnerIso2,
            String productCode,
            TradeFlow tradeFlow,
            Integer year
    ) {
        return SOURCE
                + "-"
                + reporterIso2
                + "-"
                + partnerIso2
                + "-"
                + productCode
                + "-"
                + tradeFlow.name()
                + "-"
                + year;
    }
}