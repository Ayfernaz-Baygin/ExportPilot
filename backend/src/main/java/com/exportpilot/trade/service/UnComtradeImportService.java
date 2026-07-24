package com.exportpilot.trade.service;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.country.repository.CountryRepository;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.entity.ProductCodeType;
import com.exportpilot.productcode.repository.ProductCodeRepository;
import com.exportpilot.trade.client.UnComtradeClient;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeApiResponse;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeDataRow;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeImportRequest;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeImportResponse;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradePartnerScope;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class UnComtradeImportService {

    private static final String SOURCE_NAME = "UN_COMTRADE";
    private static final int MAX_RECORDS = 100_000;

    private final UnComtradeClient unComtradeClient;
    private final CountryRepository countryRepository;
    private final ProductCodeRepository productCodeRepository;
    private final TradeRecordRepository tradeRecordRepository;

    public UnComtradeImportService(
            UnComtradeClient unComtradeClient,
            CountryRepository countryRepository,
            ProductCodeRepository productCodeRepository,
            TradeRecordRepository tradeRecordRepository
    ) {
        this.unComtradeClient = unComtradeClient;
        this.countryRepository = countryRepository;
        this.productCodeRepository = productCodeRepository;
        this.tradeRecordRepository = tradeRecordRepository;
    }

    @Transactional
    public UnComtradeImportResponse importData(
            UnComtradeImportRequest request
    ) {
        validateYearRange(
                request.startYear(),
                request.endYear()
        );

        ProductCode productCode = productCodeRepository
                .findByIdAndActiveTrue(request.productCodeId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Active product code was not found."
                ));

        validateProductCode(productCode);

        Country reporterCountry = countryRepository
                .findById(request.reporterCountryId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Reporter country was not found."
                ));

        validateM49Code(
                reporterCountry,
                "Reporter country"
        );

        Country partnerCountry = resolvePartnerCountry(
                request.partnerCountryId()
        );

        TradePartnerScope partnerScope =
                partnerCountry == null
                        ? TradePartnerScope.WORLD_TOTAL
                        : TradePartnerScope.SPECIFIC_COUNTRY;

        int partnerCode =
                partnerCountry == null
                        ? 0
                        : partnerCountry.getUnM49Code();

        TradeFlow tradeFlow = parseTradeFlow(
                request.tradeFlow()
        );

        String apiFlowCode = toApiFlowCode(tradeFlow);

        int receivedRowCount = 0;
        int createdRecordCount = 0;
        int skippedRecordCount = 0;

        for (int year = request.startYear();
             year <= request.endYear();
             year++) {

            UnComtradeApiResponse apiResponse =
                    unComtradeClient.fetchAnnualTradeData(
                            productCode.getCode(),
                            year,
                            reporterCountry.getUnM49Code(),
                            partnerCode,
                            apiFlowCode,
                            MAX_RECORDS
                    );

            List<UnComtradeDataRow> rows =
                    apiResponse.safeData();

            receivedRowCount += rows.size();

            BigDecimal totalTradeValue =
                    calculateTotalTradeValue(rows);

            BigDecimal totalQuantity =
                    calculateTotalQuantity(rows);

            BigDecimal totalNetWeight =
                    calculateTotalNetWeight(rows);

            if (totalTradeValue == null) {
                skippedRecordCount++;
                continue;
            }

            String sourceRecordId = buildSourceRecordId(
                    reporterCountry.getUnM49Code(),
                    partnerCode,
                    productCode.getCode(),
                    apiFlowCode,
                    year
            );

            boolean alreadyExists =
                    tradeRecordRepository
                            .existsBySourceAndSourceRecordId(
                                    SOURCE_NAME,
                                    sourceRecordId
                            );

            if (alreadyExists) {
                skippedRecordCount++;
                continue;
            }

            TradeRecord tradeRecord = TradeRecord.builder()
                    .source(SOURCE_NAME)
                    .reporterCountry(reporterCountry)
                    .partnerScope(partnerScope)
                    .partnerCountry(partnerCountry)
                    .productCode(productCode)
                    .tradeFlow(tradeFlow)
                    .tradeYear(year)
                    .tradeValueUsd(totalTradeValue)
                    .quantity(totalQuantity)
                    .quantityUnit(resolveQuantityUnit(rows))
                    .netWeightKg(totalNetWeight)
                    .sourceRecordId(sourceRecordId)
                    .retrievedAt(OffsetDateTime.now())
                    .build();

            tradeRecordRepository.save(tradeRecord);
            createdRecordCount++;
        }

        int requestedYearCount =
                request.endYear()
                        - request.startYear()
                        + 1;

        return new UnComtradeImportResponse(
                SOURCE_NAME,
                productCode.getId(),
                reporterCountry.getId(),
                partnerCountry == null
                        ? null
                        : partnerCountry.getId(),
                partnerScope.name(),
                tradeFlow.name(),
                request.startYear(),
                request.endYear(),
                requestedYearCount,
                receivedRowCount,
                createdRecordCount,
                skippedRecordCount
        );
    }

    private Country resolvePartnerCountry(
            Long partnerCountryId
    ) {
        if (partnerCountryId == null) {
            return null;
        }

        Country partnerCountry = countryRepository
                .findById(partnerCountryId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Partner country was not found."
                ));

        validateM49Code(
                partnerCountry,
                "Partner country"
        );

        return partnerCountry;
    }

    private void validateM49Code(
            Country country,
            String fieldName
    ) {
        if (country.getUnM49Code() == null) {
            throw new BusinessRuleException(
                    fieldName
                            + " does not have a UN M49 code."
            );
        }
    }

    private void validateProductCode(
            ProductCode productCode
    ) {
        if (productCode.getCodeType()
                != ProductCodeType.HS) {
            throw new BusinessRuleException(
                    "UN Comtrade import currently supports only HS product codes."
            );
        }

        if (!StringUtils.hasText(productCode.getCode())) {
            throw new BusinessRuleException(
                    "Product code cannot be empty."
            );
        }
    }

    private void validateYearRange(
            Integer startYear,
            Integer endYear
    ) {
        if (startYear == null || endYear == null) {
            throw new BusinessRuleException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }

        if (endYear - startYear > 10) {
            throw new BusinessRuleException(
                    "A maximum of 11 years can be imported at once."
            );
        }
    }

    private TradeFlow parseTradeFlow(
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessRuleException(
                    "Trade flow is required."
            );
        }

        try {
            return TradeFlow.valueOf(
                    value.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(
                    "Trade flow must be IMPORT or EXPORT."
            );
        }
    }

    private String toApiFlowCode(
            TradeFlow tradeFlow
    ) {
        return switch (tradeFlow) {
            case IMPORT -> "M";
            case EXPORT -> "X";
        };
    }

    private BigDecimal calculateTotalTradeValue(
            List<UnComtradeDataRow> rows
    ) {
        List<UnComtradeDataRow> usableRows =
                selectUsableRows(rows);

        BigDecimal total = usableRows.stream()
                .map(UnComtradeDataRow::primaryValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(BigDecimal.ZERO) > 0
                ? total
                : null;
    }

    private BigDecimal calculateTotalQuantity(
            List<UnComtradeDataRow> rows
    ) {
        BigDecimal total = selectUsableRows(rows).stream()
                .map(UnComtradeDataRow::qty)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(BigDecimal.ZERO) > 0
                ? total
                : null;
    }

    private BigDecimal calculateTotalNetWeight(
            List<UnComtradeDataRow> rows
    ) {
        BigDecimal total = selectUsableRows(rows).stream()
                .map(UnComtradeDataRow::netWgt)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(BigDecimal.ZERO) > 0
                ? total
                : null;
    }

    private List<UnComtradeDataRow> selectUsableRows(
            List<UnComtradeDataRow> rows
    ) {
        List<UnComtradeDataRow> aggregateRows =
                rows.stream()
                        .filter(row ->
                                Boolean.TRUE.equals(
                                        row.isAggregate()
                                )
                        )
                        .toList();

        if (!aggregateRows.isEmpty()) {
            return aggregateRows;
        }

        return rows.stream()
                .filter(row ->
                        !Boolean.FALSE.equals(
                                row.isReported()
                        )
                )
                .toList();
    }

    private String resolveQuantityUnit(
            List<UnComtradeDataRow> rows
    ) {
        return selectUsableRows(rows).stream()
                .map(UnComtradeDataRow::qtyUnitAbbr)
                .filter(StringUtils::hasText)
                .distinct()
                .reduce((first, second) ->
                        first.equalsIgnoreCase(second)
                                ? first
                                : null
                )
                .orElse(null);
    }

    private String buildSourceRecordId(
            Integer reporterCode,
            Integer partnerCode,
            String commodityCode,
            String flowCode,
            Integer year
    ) {
        return String.join(
                ":",
                SOURCE_NAME,
                reporterCode.toString(),
                partnerCode.toString(),
                commodityCode,
                flowCode,
                year.toString()
        );
    }
}