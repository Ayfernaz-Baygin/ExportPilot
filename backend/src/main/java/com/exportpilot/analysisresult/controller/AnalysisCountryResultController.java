package com.exportpilot.analysisresult.controller;

import com.exportpilot.analysisresult.dto.AnalysisCountryResultResponse;
import com.exportpilot.analysisresult.service.AnalysisCountryResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis-results")
@Tag(
        name = "Analysis Results",
        description = "Country-level export analysis result operations"
)
public class AnalysisCountryResultController {

    private final AnalysisCountryResultService resultService;

    public AnalysisCountryResultController(
            AnalysisCountryResultService resultService
    ) {
        this.resultService = resultService;
    }

    @Operation(
            summary = "List results by analysis",
            description = "Returns country results ordered by rank."
    )
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<List<AnalysisCountryResultResponse>>
    getResultsByAnalysisId(
            @PathVariable Long analysisId
    ) {
        return ResponseEntity.ok(
                resultService.getResultsByAnalysisId(analysisId)
        );
    }

    @Operation(
            summary = "Get analysis result by ID",
            description = "Returns a single country-level analysis result."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisCountryResultResponse> getResultById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                resultService.getResultById(id)
        );
    }

    @Operation(
            summary = "Get analysis result by country",
            description = "Returns the result for a country within an analysis."
    )
    @GetMapping("/analysis/{analysisId}/country/{countryId}")
    public ResponseEntity<AnalysisCountryResultResponse>
    getResultByAnalysisAndCountry(
            @PathVariable Long analysisId,
            @PathVariable Long countryId
    ) {
        return ResponseEntity.ok(
                resultService.getResultByAnalysisAndCountry(
                        analysisId,
                        countryId
                )
        );
    }
}