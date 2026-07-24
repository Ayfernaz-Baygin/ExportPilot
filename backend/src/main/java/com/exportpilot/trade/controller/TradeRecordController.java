package com.exportpilot.trade.controller;

import com.exportpilot.trade.dto.TradeCsvImportResponse;
import com.exportpilot.trade.dto.TradeDataFetchResponse;
import com.exportpilot.trade.dto.TradeRecordResponse;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeImportRequest;
import com.exportpilot.trade.dto.uncomtrade.UnComtradeImportResponse;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.provider.TradeDataSourceType;
import com.exportpilot.trade.service.TradeCsvImportService;
import com.exportpilot.trade.service.TradeDataImportService;
import com.exportpilot.trade.service.TradeRecordService;
import com.exportpilot.trade.service.UnComtradeImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade-records")
@Tag(
        name = "Trade Records",
        description = "International trade data operations"
)
public class TradeRecordController {

    private final TradeRecordService tradeRecordService;
    private final TradeDataImportService tradeDataImportService;
    private final TradeCsvImportService tradeCsvImportService;
    private final UnComtradeImportService unComtradeImportService;

    public TradeRecordController(
            TradeRecordService tradeRecordService,
            TradeDataImportService tradeDataImportService,
            TradeCsvImportService tradeCsvImportService,
            UnComtradeImportService unComtradeImportService
    ) {
        this.tradeRecordService = tradeRecordService;
        this.tradeDataImportService = tradeDataImportService;
        this.tradeCsvImportService = tradeCsvImportService;
        this.unComtradeImportService = unComtradeImportService;
    }

    @Operation(
            summary = "List trade records",
            description = "Returns all stored trade records."
    )
    @GetMapping
    public ResponseEntity<List<TradeRecordResponse>>
    getTradeRecords() {
        return ResponseEntity.ok(
                tradeRecordService.getTradeRecords()
        );
    }

    @Operation(
            summary = "Get trade record by ID",
            description = "Returns the requested trade record."
    )
    @GetMapping("/{id}")
    public ResponseEntity<TradeRecordResponse>
    getTradeRecordById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                tradeRecordService.getTradeRecordById(id)
        );
    }

    @Operation(
            summary = "Search trade records",
            description = """
                    Returns trade records matching product code,
                    reporter country, trade flow and year range.
                    """
    )
    @GetMapping("/search")
    public ResponseEntity<List<TradeRecordResponse>>
    searchTradeRecords(
            @RequestParam Long productCodeId,
            @RequestParam Long reporterCountryId,
            @RequestParam TradeFlow tradeFlow,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear
    ) {
        return ResponseEntity.ok(
                tradeRecordService.searchTradeRecords(
                        productCodeId,
                        reporterCountryId,
                        tradeFlow,
                        startYear,
                        endYear
                )
        );
    }

    @Operation(
            summary = "Fetch trade data for an analysis",
            description = """
                    Fetches trade records for the selected analysis
                    using the requested data source.

                    SAMPLE generates synthetic development records.

                    UN_COMTRADE retrieves official trade data for
                    Germany, Poland and Romania, including world totals
                    and imports originating from Türkiye.

                    CSV must be imported using the CSV upload endpoint.

                    ITC_TRADE_MAP is not available yet.
                    """
    )
    @PostMapping("/fetch/analysis/{analysisId}")
    public ResponseEntity<TradeDataFetchResponse>
    fetchTradeDataForAnalysis(
            @PathVariable Long analysisId,
            @RequestParam(defaultValue = "SAMPLE")
            TradeDataSourceType sourceType
    ) {
        return ResponseEntity.ok(
                tradeDataImportService.fetchForAnalysis(
                        analysisId,
                        sourceType
                )
        );
    }

    @Operation(
            summary = "Import CSV trade data for an analysis",
            description = """
                    Imports trade records from a CSV file using the
                    selected analysis product code and year range.

                    Existing records with the same source record ID are
                    skipped.

                    Invalid CSV rows are rejected and reported in the
                    response.
                    """
    )
    @PostMapping(
            value = "/import/csv/analysis/{analysisId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TradeCsvImportResponse>
    importCsvForAnalysis(
            @PathVariable Long analysisId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                tradeCsvImportService.importForAnalysis(
                        analysisId,
                        file
                )
        );
    }

    @Operation(
            summary = "Import trade data from UN Comtrade",
            description = """
                    Fetches annual trade data from the UN Comtrade API
                    and stores an aggregated trade record.

                    If partnerCountryId is null, world total data is imported.

                    Existing records with the same source and source record ID
                    are skipped.
                    """
    )
    @PostMapping("/import/un-comtrade")
    public ResponseEntity<UnComtradeImportResponse>
    importFromUnComtrade(
            @Valid
            @RequestBody
            UnComtradeImportRequest request
    ) {
        return ResponseEntity.ok(
                unComtradeImportService.importData(request)
        );
    }
}