package com.exportpilot.economicindicator.controller;

import com.exportpilot.economicindicator.dto.AnalysisEconomicDataFetchResponse;
import com.exportpilot.economicindicator.dto.EconomicIndicatorFetchResponse;
import com.exportpilot.economicindicator.dto.EconomicIndicatorResponse;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import com.exportpilot.economicindicator.service.EconomicIndicatorBatchService;
import com.exportpilot.economicindicator.service.EconomicIndicatorService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/economic-indicators")
@Validated
public class EconomicIndicatorController {

    private final EconomicIndicatorService economicIndicatorService;

    private final EconomicIndicatorBatchService
            economicIndicatorBatchService;

    public EconomicIndicatorController(
            EconomicIndicatorService economicIndicatorService,
            EconomicIndicatorBatchService economicIndicatorBatchService
    ) {
        this.economicIndicatorService =
                economicIndicatorService;

        this.economicIndicatorBatchService =
                economicIndicatorBatchService;
    }

    @PostMapping("/fetch/country/{countryId}")
    public ResponseEntity<EconomicIndicatorFetchResponse>
    fetchCountryIndicators(

            @PathVariable
            Long countryId,

            @RequestParam
            @Min(1900)
            @Max(2100)
            Integer startYear,

            @RequestParam
            @Min(1900)
            @Max(2100)
            Integer endYear
    ) {
        EconomicIndicatorFetchResponse response =
                economicIndicatorService
                        .fetchCountryIndicators(
                                countryId,
                                startYear,
                                endYear
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/fetch/analysis/{analysisId}")
    public ResponseEntity<AnalysisEconomicDataFetchResponse>
    fetchAnalysisIndicators(
            @PathVariable Long analysisId
    ) {
        AnalysisEconomicDataFetchResponse response =
                economicIndicatorBatchService
                        .fetchIndicatorsForAnalysis(
                                analysisId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<EconomicIndicatorResponse>>
    getCountryIndicators(
            @PathVariable Long countryId
    ) {
        return ResponseEntity.ok(
                economicIndicatorService
                        .getCountryIndicators(countryId)
        );
    }

    @GetMapping(
            "/country/{countryId}/indicator/{indicatorType}"
    )
    public ResponseEntity<List<EconomicIndicatorResponse>>
    getCountryIndicatorHistory(

            @PathVariable
            Long countryId,

            @PathVariable
            EconomicIndicatorType indicatorType
    ) {
        return ResponseEntity.ok(
                economicIndicatorService
                        .getCountryIndicatorHistory(
                                countryId,
                                indicatorType
                        )
        );
    }

    @GetMapping(
            "/country/{countryId}/indicator/{indicatorType}/latest"
    )
    public ResponseEntity<EconomicIndicatorResponse>
    getLatestCountryIndicator(

            @PathVariable
            Long countryId,

            @PathVariable
            EconomicIndicatorType indicatorType
    ) {
        return ResponseEntity.ok(
                economicIndicatorService
                        .getLatestCountryIndicator(
                                countryId,
                                indicatorType
                        )
        );
    }
}