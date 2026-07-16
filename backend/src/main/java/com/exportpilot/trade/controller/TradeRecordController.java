package com.exportpilot.trade.controller;

import com.exportpilot.trade.dto.TradeRecordResponse;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.service.TradeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade-records")
@Tag(
        name = "Trade Records",
        description = "International trade data operations"
)
public class TradeRecordController {

    private final TradeRecordService tradeRecordService;

    public TradeRecordController(
            TradeRecordService tradeRecordService
    ) {
        this.tradeRecordService = tradeRecordService;
    }

    @Operation(
            summary = "List trade records",
            description = "Returns all stored trade records."
    )
    @GetMapping
    public ResponseEntity<List<TradeRecordResponse>> getTradeRecords() {
        return ResponseEntity.ok(
                tradeRecordService.getTradeRecords()
        );
    }

    @Operation(
            summary = "Get trade record by ID",
            description = "Returns the requested trade record."
    )
    @GetMapping("/{id}")
    public ResponseEntity<TradeRecordResponse> getTradeRecordById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                tradeRecordService.getTradeRecordById(id)
        );
    }

    @Operation(
            summary = "Search trade records",
            description = "Returns trade records matching product code, reporter country, trade flow and year range."
    )
    @GetMapping("/search")
    public ResponseEntity<List<TradeRecordResponse>> searchTradeRecords(
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
}