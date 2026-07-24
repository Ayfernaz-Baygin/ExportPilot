package com.exportpilot.trade.service;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.entity.Country;
import com.exportpilot.country.repository.CountryRepository;
import com.exportpilot.trade.dto.TradeDataFetchResponse;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.provider.TradeDataProvider;
import com.exportpilot.trade.provider.TradeDataProviderFactory;
import com.exportpilot.trade.provider.TradeDataSourceType;
import com.exportpilot.trade.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeDataImportService {

    private final AnalysisRepository analysisRepository;
    private final CountryRepository countryRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final TradeDataProviderFactory tradeDataProviderFactory;

    public TradeDataImportService(
            AnalysisRepository analysisRepository,
            CountryRepository countryRepository,
            TradeRecordRepository tradeRecordRepository,
            TradeDataProviderFactory tradeDataProviderFactory
    ) {
        this.analysisRepository =
                analysisRepository;

        this.countryRepository =
                countryRepository;

        this.tradeRecordRepository =
                tradeRecordRepository;

        this.tradeDataProviderFactory =
                tradeDataProviderFactory;
    }

    @Transactional
    public TradeDataFetchResponse fetchForAnalysis(
            Long analysisId
    ) {
        return fetchForAnalysis(
                analysisId,
                TradeDataSourceType.SAMPLE
        );
    }

    @Transactional
    public TradeDataFetchResponse fetchForAnalysis(
            Long analysisId,
            TradeDataSourceType sourceType
    ) {
        if (analysisId == null) {
            throw new BusinessRuleException(
                    "Analysis ID is required."
            );
        }

        if (sourceType == null) {
            throw new BusinessRuleException(
                    "Trade data source type is required."
            );
        }

        Analysis analysis =
                analysisRepository
                        .findById(analysisId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Analysis not found with id: "
                                                + analysisId
                                )
                        );

        validateAnalysis(
                analysis,
                sourceType
        );

        Country germany =
                getCountry("DE");

        Country poland =
                getCountry("PL");

        Country romania =
                getCountry("RO");

        Country turkey =
                getCountry("TR");

        TradeDataProvider tradeDataProvider =
                tradeDataProviderFactory
                        .getProvider(sourceType);

        List<TradeRecord> generatedRecords =
                tradeDataProvider.generateRecords(
                        analysis.getProductCode(),
                        analysis.getStartYear(),
                        analysis.getEndYear(),
                        germany,
                        poland,
                        romania,
                        turkey
                );

        validateGeneratedRecords(
                generatedRecords,
                analysis,
                sourceType
        );

        List<TradeRecord> recordsToSave =
                new ArrayList<>();

        int skippedRecordCount = 0;

        for (TradeRecord generatedRecord
                : generatedRecords) {

            if (generatedRecord == null) {
                continue;
            }

            boolean alreadyExists =
                    tradeRecordRepository
                            .existsBySourceAndSourceRecordId(
                                    generatedRecord.getSource(),
                                    generatedRecord
                                            .getSourceRecordId()
                            );

            if (alreadyExists) {
                skippedRecordCount++;
                continue;
            }

            recordsToSave.add(
                    generatedRecord
            );
        }

        if (!recordsToSave.isEmpty()) {
            tradeRecordRepository.saveAll(
                    recordsToSave
            );

            tradeRecordRepository.flush();
        }

        return new TradeDataFetchResponse(
                analysis.getId(),
                analysis.getProductCode().getId(),
                analysis.getProductCode().getCode(),
                analysis.getStartYear(),
                analysis.getEndYear(),
                recordsToSave.size(),
                skippedRecordCount,
                tradeDataProvider.getSourceName(),
                buildMessage(
                        sourceType,
                        recordsToSave.size(),
                        skippedRecordCount
                )
        );
    }

    private Country getCountry(
            String iso2Code
    ) {
        return countryRepository
                .findByIso2CodeIgnoreCase(
                        iso2Code
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with ISO2 code: "
                                        + iso2Code
                        )
                );
    }

    private void validateAnalysis(
            Analysis analysis,
            TradeDataSourceType sourceType
    ) {
        if (analysis.getProductCode() == null) {
            throw new BusinessRuleException(
                    "Analysis does not contain a product code."
            );
        }

        if (analysis.getStartYear() == null
                || analysis.getEndYear() == null) {
            throw new BusinessRuleException(
                    "Analysis year range is incomplete."
            );
        }

        if (analysis.getStartYear()
                > analysis.getEndYear()) {
            throw new BusinessRuleException(
                    "Analysis start year cannot be greater than end year."
            );
        }

        int yearCount =
                analysis.getEndYear()
                        - analysis.getStartYear()
                        + 1;

        if (sourceType == TradeDataSourceType.SAMPLE
                && yearCount > 10) {
            throw new BusinessRuleException(
                    "Sample trade data generation supports "
                            + "a maximum of 10 years."
            );
        }

        if (sourceType == TradeDataSourceType.UN_COMTRADE
                && yearCount > 5) {
            throw new BusinessRuleException(
                    "UN Comtrade analysis import supports "
                            + "a maximum of 5 years per request."
            );
        }

        if (sourceType == TradeDataSourceType.CSV) {
            throw new BusinessRuleException(
                    "CSV data must be imported through "
                            + "the CSV upload endpoint."
            );
        }

        if (sourceType
                == TradeDataSourceType.ITC_TRADE_MAP) {
            throw new BusinessRuleException(
                    "ITC Trade Map integration "
                            + "is not available yet."
            );
        }
    }

    private void validateGeneratedRecords(
            List<TradeRecord> generatedRecords,
            Analysis analysis,
            TradeDataSourceType sourceType
    ) {
        if (generatedRecords != null
                && !generatedRecords.isEmpty()) {
            return;
        }

        if (sourceType
                == TradeDataSourceType.UN_COMTRADE) {
            throw new BusinessRuleException(
                    "UN Comtrade returned no usable trade records "
                            + "for product code "
                            + analysis.getProductCode().getCode()
                            + " and year range "
                            + analysis.getStartYear()
                            + "-"
                            + analysis.getEndYear()
                            + "."
            );
        }

        throw new BusinessRuleException(
                sourceType
                        + " returned no usable trade records "
                        + "for the selected analysis."
        );
    }

    private String buildMessage(
            TradeDataSourceType sourceType,
            int createdRecordCount,
            int skippedRecordCount
    ) {
        if (createdRecordCount == 0
                && skippedRecordCount > 0) {
            return "All trade records from "
                    + sourceType
                    + " already existed. "
                    + "No new records were inserted.";
        }

        if (createdRecordCount == 0) {
            return "No new trade records from "
                    + sourceType
                    + " were inserted.";
        }

        return createdRecordCount
                + " trade records from "
                + sourceType
                + " were inserted. "
                + skippedRecordCount
                + " existing records were skipped.";
    }
}