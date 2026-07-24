package com.exportpilot.trade.service;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.trade.dto.TradeCsvImportResponse;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.provider.CsvTradeDataProvider;
import com.exportpilot.trade.provider.CsvTradeParseResult;
import com.exportpilot.trade.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeCsvImportService {

    private final AnalysisRepository analysisRepository;
    private final TradeRecordRepository tradeRecordRepository;
    private final CsvTradeDataProvider csvTradeDataProvider;

    public TradeCsvImportService(
            AnalysisRepository analysisRepository,
            TradeRecordRepository tradeRecordRepository,
            CsvTradeDataProvider csvTradeDataProvider
    ) {
        this.analysisRepository = analysisRepository;
        this.tradeRecordRepository =
                tradeRecordRepository;
        this.csvTradeDataProvider =
                csvTradeDataProvider;
    }

    @Transactional
    public TradeCsvImportResponse importForAnalysis(
            Long analysisId,
            MultipartFile file
    ) {
        if (analysisId == null) {
            throw new BusinessRuleException(
                    "Analysis ID is required."
            );
        }

        Analysis analysis = analysisRepository
                .findById(analysisId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis not found with id: "
                                        + analysisId
                        )
                );

        validateAnalysis(analysis);

        CsvTradeParseResult parseResult =
                csvTradeDataProvider.parseFile(
                        file,
                        analysis.getProductCode(),
                        analysis.getStartYear(),
                        analysis.getEndYear()
                );

        List<TradeRecord> recordsToSave =
                new ArrayList<>();

        int skippedRecordCount = 0;

        for (TradeRecord tradeRecord
                : parseResult.records()) {

            boolean alreadyExists =
                    tradeRecordRepository
                            .existsBySourceRecordId(
                                    tradeRecord
                                            .getSourceRecordId()
                            );

            if (alreadyExists) {
                skippedRecordCount++;
                continue;
            }

            recordsToSave.add(tradeRecord);
        }

        if (!recordsToSave.isEmpty()) {
            tradeRecordRepository.saveAll(
                    recordsToSave
            );
        }

        return new TradeCsvImportResponse(
                analysis.getId(),
                analysis.getProductCode().getId(),
                analysis.getProductCode().getCode(),
                analysis.getStartYear(),
                analysis.getEndYear(),
                file.getOriginalFilename(),
                recordsToSave.size(),
                skippedRecordCount,
                parseResult.invalidRecordCount(),
                csvTradeDataProvider.getSourceName(),
                parseResult.errors(),
                buildMessage(
                        recordsToSave.size(),
                        skippedRecordCount,
                        parseResult.invalidRecordCount()
                )
        );
    }

    private void validateAnalysis(Analysis analysis) {
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
    }

    private String buildMessage(
            int createdCount,
            int skippedCount,
            int invalidCount
    ) {
        return createdCount
                + " CSV trade records were inserted. "
                + skippedCount
                + " existing records were skipped. "
                + invalidCount
                + " invalid rows were rejected.";
    }
}