package com.exportpilot.trade.provider;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.entity.ProductCodeType;
import com.exportpilot.trade.client.UnComtradeClient;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeApiResponse;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeDataRow;
import com.exportpilot.trade.entity.TradeDataStatus;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradePartnerScope;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.entity.TradeRevisionStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class UnComtradeTradeDataProvider
        implements TradeDataProvider {

    private static final String SOURCE =
            "UN_COMTRADE";

    private static final String IMPORT_FLOW_CODE =
            "M";

    private static final int WORLD_PARTNER_CODE =
            0;

    private static final int MAX_RECORDS =
            100_000;

    private static final int HS6_LENGTH =
            6;

    private static final int MIN_HS_LENGTH =
            2;

    private final UnComtradeClient unComtradeClient;

    public UnComtradeTradeDataProvider(
            UnComtradeClient unComtradeClient
    ) {
        this.unComtradeClient =
                unComtradeClient;
    }

    @Override
    public TradeDataSourceType getType() {
        return TradeDataSourceType.UN_COMTRADE;
    }

    @Override
    public String getSourceName() {
        return SOURCE;
    }

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

        String unComtradeHsCode =
                resolveUnComtradeHsCode(
                        productCode
                );

        List<Integer> years =
                buildYears(
                        startYear,
                        endYear
                );

        List<Country> targetCountries =
                List.of(
                        germany,
                        poland,
                        romania
                );

        List<TradeRecord> records =
                new ArrayList<>();

        for (Country reporterCountry
                : targetCountries) {

            importWorldTotalRecords(
                    records,
                    productCode,
                    unComtradeHsCode,
                    years,
                    reporterCountry
            );

            importTurkeyPartnerRecords(
                    records,
                    productCode,
                    unComtradeHsCode,
                    years,
                    reporterCountry,
                    turkey
            );
        }

        return records;
    }

    private void importWorldTotalRecords(
            List<TradeRecord> records,
            ProductCode productCode,
            String unComtradeHsCode,
            List<Integer> years,
            Country reporterCountry
    ) {
        UnComtradeApiResponse response =
                unComtradeClient.fetchAnnualTradeData(
                        unComtradeHsCode,
                        years,
                        reporterCountry.getUnM49Code(),
                        WORLD_PARTNER_CODE,
                        IMPORT_FLOW_CODE,
                        MAX_RECORDS
                );

        List<UnComtradeDataRow> responseRows =
                response.safeData();

        for (Integer year : years) {
            List<UnComtradeDataRow> yearRows =
                    filterRowsByYear(
                            responseRows,
                            year
                    );

            AggregatedValues values =
                    aggregateRows(
                            yearRows,
                            unComtradeHsCode
                    );

            if (values.tradeValueUsd() == null) {
                continue;
            }

            records.add(
                    createTradeRecord(
                            productCode,
                            unComtradeHsCode,
                            reporterCountry,
                            null,
                            TradePartnerScope.WORLD_TOTAL,
                            WORLD_PARTNER_CODE,
                            year,
                            values
                    )
            );
        }
    }

    private void importTurkeyPartnerRecords(
            List<TradeRecord> records,
            ProductCode productCode,
            String unComtradeHsCode,
            List<Integer> years,
            Country reporterCountry,
            Country turkey
    ) {
        UnComtradeApiResponse response =
                unComtradeClient.fetchAnnualTradeData(
                        unComtradeHsCode,
                        years,
                        reporterCountry.getUnM49Code(),
                        turkey.getUnM49Code(),
                        IMPORT_FLOW_CODE,
                        MAX_RECORDS
                );

        List<UnComtradeDataRow> responseRows =
                response.safeData();

        for (Integer year : years) {
            List<UnComtradeDataRow> yearRows =
                    filterRowsByYear(
                            responseRows,
                            year
                    );

            AggregatedValues values =
                    aggregateRows(
                            yearRows,
                            unComtradeHsCode
                    );

            if (values.tradeValueUsd() == null) {
                continue;
            }

            records.add(
                    createTradeRecord(
                            productCode,
                            unComtradeHsCode,
                            reporterCountry,
                            turkey,
                            TradePartnerScope.SPECIFIC_COUNTRY,
                            turkey.getUnM49Code(),
                            year,
                            values
                    )
            );
        }
    }

    private TradeRecord createTradeRecord(
            ProductCode productCode,
            String unComtradeHsCode,
            Country reporterCountry,
            Country partnerCountry,
            TradePartnerScope partnerScope,
            Integer partnerM49Code,
            Integer year,
            AggregatedValues values
    ) {
        return TradeRecord.builder()
                .source(SOURCE)
                .reporterCountry(
                        reporterCountry
                )
                .partnerScope(
                        partnerScope
                )
                .partnerCountry(
                        partnerCountry
                )
                .productCode(
                        productCode
                )
                .tradeFlow(
                        TradeFlow.IMPORT
                )
                .tradeYear(
                        year
                )
                .tradeValueUsd(
                        values.tradeValueUsd()
                )
                .quantity(
                        values.quantity()
                )
                .quantityUnit(
                        values.quantityUnit()
                )
                .netWeightKg(
                        values.netWeightKg()
                )
                .sourceRecordId(
                        buildSourceRecordId(
                                reporterCountry
                                        .getUnM49Code(),
                                partnerM49Code,
                                productCode,
                                unComtradeHsCode,
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

    private List<UnComtradeDataRow> filterRowsByYear(
            List<UnComtradeDataRow> rows,
            Integer requestedYear
    ) {
        if (rows == null
                || rows.isEmpty()) {
            return List.of();
        }

        return rows.stream()
                .filter(
                        Objects::nonNull
                )
                .filter(row ->
                        requestedYear.equals(
                                resolveRowYear(
                                        row
                                )
                        )
                )
                .toList();
    }

    private Integer resolveRowYear(
            UnComtradeDataRow row
    ) {
        if (row.refYear() != null) {
            return row.refYear();
        }

        if (!StringUtils.hasText(
                row.period()
        )) {
            return null;
        }

        String normalizedPeriod =
                row.period()
                        .replaceAll(
                                "\\D",
                                ""
                        );

        if (normalizedPeriod.length() < 4) {
            return null;
        }

        try {
            return Integer.valueOf(
                    normalizedPeriod.substring(
                            0,
                            4
                    )
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private AggregatedValues aggregateRows(
            List<UnComtradeDataRow> rows,
            String requestedHsCode
    ) {
        List<UnComtradeDataRow> selectedRows =
                selectRows(
                        rows,
                        requestedHsCode
                );

        BigDecimal tradeValueUsd =
                sumPositiveValues(
                        selectedRows.stream()
                                .map(
                                        UnComtradeDataRow
                                                ::primaryValue
                                )
                                .toList()
                );

        BigDecimal quantity =
                sumPositiveValues(
                        selectedRows.stream()
                                .map(
                                        UnComtradeDataRow
                                                ::qty
                                )
                                .toList()
                );

        BigDecimal netWeightKg =
                sumPositiveValues(
                        selectedRows.stream()
                                .map(
                                        UnComtradeDataRow
                                                ::netWgt
                                )
                                .toList()
                );

        String quantityUnit =
                resolveQuantityUnit(
                        selectedRows
                );

        return new AggregatedValues(
                tradeValueUsd,
                quantity,
                quantityUnit,
                netWeightKg
        );
    }

    private List<UnComtradeDataRow> selectRows(
            List<UnComtradeDataRow> rows,
            String requestedHsCode
    ) {
        if (rows == null
                || rows.isEmpty()) {
            return List.of();
        }

        List<UnComtradeDataRow> matchingCodeRows =
                rows.stream()
                        .filter(
                                Objects::nonNull
                        )
                        .filter(row ->
                                requestedHsCode.equals(
                                        normalizeCommodityCode(
                                                row.cmdCode()
                                        )
                                )
                        )
                        .toList();

        if (matchingCodeRows.isEmpty()) {
            return List.of();
        }

        /*
         * 1. Öncelik:
         * Tam toplam boyutları.
         */
        List<UnComtradeDataRow> exactTotalRows =
                matchingCodeRows.stream()
                        .filter(row ->
                                row.partner2Code() == null
                                        || row.partner2Code() == 0
                        )
                        .filter(row ->
                                !StringUtils.hasText(
                                        row.customsCode()
                                )
                                        || "C00".equalsIgnoreCase(
                                                row.customsCode()
                                        )
                        )
                        .filter(row ->
                                !StringUtils.hasText(
                                        row.motCode()
                                )
                                        || "0".equals(
                                                row.motCode()
                                        )
                        )
                        .filter(row ->
                                !Boolean.FALSE.equals(
                                        row.isReported()
                                )
                        )
                        .toList();

        if (!exactTotalRows.isEmpty()) {
            return exactTotalRows;
        }

        /*
         * 2. Öncelik:
         * Partner2 toplamı olan bildirilmiş satırlar.
         * Customs ve taşıma yöntemi filtresi uygulanmaz.
         */
        List<UnComtradeDataRow> partnerTotalRows =
                matchingCodeRows.stream()
                        .filter(row ->
                                row.partner2Code() == null
                                        || row.partner2Code() == 0
                        )
                        .filter(row ->
                                !Boolean.FALSE.equals(
                                        row.isReported()
                                )
                        )
                        .toList();

        if (!partnerTotalRows.isEmpty()) {
            return partnerTotalRows;
        }

        /*
         * 3. Öncelik:
         * Kodla eşleşen ve bildirilmiş tüm satırlar.
         */
        List<UnComtradeDataRow> reportedRows =
                matchingCodeRows.stream()
                        .filter(row ->
                                !Boolean.FALSE.equals(
                                        row.isReported()
                                )
                        )
                        .toList();

        if (!reportedRows.isEmpty()) {
            return reportedRows;
        }

        /*
         * 4. Son seçenek:
         * Kodla eşleşen ve pozitif ticaret değeri bulunan satırlar.
         */
        return matchingCodeRows.stream()
                .filter(row ->
                        row.primaryValue() != null
                                && row.primaryValue()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) > 0
                )
                .toList();
    }

    private BigDecimal sumPositiveValues(
            List<BigDecimal> values
    ) {
        BigDecimal total =
                values.stream()
                        .filter(
                                Objects::nonNull
                        )
                        .filter(value ->
                                value.compareTo(
                                        BigDecimal.ZERO
                                ) > 0
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (total.compareTo(
                BigDecimal.ZERO
        ) <= 0) {
            return null;
        }

        return total;
    }

    private String resolveQuantityUnit(
            List<UnComtradeDataRow> rows
    ) {
        List<String> units =
                rows.stream()
                        .map(
                                UnComtradeDataRow
                                        ::qtyUnitAbbr
                        )
                        .filter(
                                StringUtils::hasText
                        )
                        .distinct()
                        .toList();

        if (units.size() != 1) {
            return null;
        }

        return units.getFirst();
    }

    private List<Integer> buildYears(
            Integer startYear,
            Integer endYear
    ) {
        return IntStream.rangeClosed(
                        startYear,
                        endYear
                )
                .boxed()
                .toList();
    }

    private String resolveUnComtradeHsCode(
            ProductCode productCode
    ) {
        String normalizedCode =
                normalizeCommodityCode(
                        productCode.getCode()
                );

        if (productCode.getCodeType()
                == ProductCodeType.GTIP) {

            if (normalizedCode.length()
                    < HS6_LENGTH) {
                throw new BusinessRuleException(
                        "GTIP code must contain at least 6 digits."
                );
            }

            return normalizedCode.substring(
                    0,
                    HS6_LENGTH
            );
        }

        if (productCode.getCodeType()
                == ProductCodeType.HS) {

            if (normalizedCode.length()
                    < MIN_HS_LENGTH
                    || normalizedCode.length()
                    > HS6_LENGTH) {
                throw new BusinessRuleException(
                        "HS code must contain between 2 and 6 digits."
                );
            }

            return normalizedCode;
        }

        throw new BusinessRuleException(
                "UN Comtrade import requires a GTIP or HS product code."
        );
    }

    private String normalizeCommodityCode(
            String code
    ) {
        if (!StringUtils.hasText(
                code
        )) {
            return "";
        }

        return code.replaceAll(
                "\\D",
                ""
        );
    }

    private String buildSourceRecordId(
            Integer reporterCode,
            Integer partnerCode,
            ProductCode productCode,
            String unComtradeHsCode,
            Integer year
    ) {
        String normalizedOriginalCode =
                normalizeCommodityCode(
                        productCode.getCode()
                );

        return String.join(
                ":",
                SOURCE,
                "REPORTER-" + reporterCode,
                "PARTNER-" + partnerCode,
                "CODE_TYPE-"
                        + productCode.getCodeType(),
                "ORIGINAL_CODE-"
                        + normalizedOriginalCode,
                "HS_CODE-"
                        + unComtradeHsCode,
                "FLOW-"
                        + IMPORT_FLOW_CODE,
                "YEAR-"
                        + year
        );
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
            throw new BusinessRuleException(
                    "Product code is required."
            );
        }

        if (productCode.getCodeType()
                != ProductCodeType.GTIP
                && productCode.getCodeType()
                != ProductCodeType.HS) {
            throw new BusinessRuleException(
                    "UN Comtrade import requires a GTIP or HS product code."
            );
        }

        if (!StringUtils.hasText(
                productCode.getCode()
        )) {
            throw new BusinessRuleException(
                    "Product code cannot be empty."
            );
        }

        resolveUnComtradeHsCode(
                productCode
        );

        if (startYear == null
                || endYear == null) {
            throw new BusinessRuleException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }

        validateCountry(
                germany,
                "Germany"
        );

        validateCountry(
                poland,
                "Poland"
        );

        validateCountry(
                romania,
                "Romania"
        );

        validateCountry(
                turkey,
                "Türkiye"
        );
    }

    private void validateCountry(
            Country country,
            String countryName
    ) {
        if (country == null) {
            throw new BusinessRuleException(
                    countryName + " is required."
            );
        }

        if (country.getUnM49Code() == null) {
            throw new BusinessRuleException(
                    countryName
                            + " does not have a UN M49 code."
            );
        }
    }

    private record AggregatedValues(
            BigDecimal tradeValueUsd,
            BigDecimal quantity,
            String quantityUnit,
            BigDecimal netWeightKg
    ) {
    }
}