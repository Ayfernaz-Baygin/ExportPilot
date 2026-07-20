package com.exportpilot.analysisresult.controller;

import com.exportpilot.analysisresult.dto.AnalysisCountryResultResponse;
import com.exportpilot.analysisresult.interpretation.CountryAnalysisInterpretation;
import com.exportpilot.analysisresult.service.AnalysisCountryResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exportpilot.analysisresult.ai.AiMarketReport;
import com.exportpilot.analysisresult.ai.AiMarketReportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis-results")
@Tag(
        name = "Analysis Results",
        description = "Country-level export analysis result operations"
)
public class AnalysisCountryResultController {

    private final AnalysisCountryResultService resultService;


    private final AiMarketReportService aiMarketReportService;

    public AnalysisCountryResultController(
        AnalysisCountryResultService resultService,
        AiMarketReportService aiMarketReportService
) {
    this.resultService = resultService;
    this.aiMarketReportService = aiMarketReportService;
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
            summary = "Get analysis result interpretation",
            description = """
                    Returns the recommendation level, summary,
                    strengths, risks and decision for a country result.
                    """
    )
    @GetMapping("/{id}/interpretation")
    public ResponseEntity<CountryAnalysisInterpretation>
    getInterpretationByResultId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                resultService.getInterpretationByResultId(id)
        );
    }

    @Operation(
        summary = "Generate AI market report",
        description = """
                Generates a professional AI-supported market report
                using deterministic country analysis results.
                """
)
@GetMapping("/analysis/{analysisId}/ai-report")
public ResponseEntity<AiMarketReport> generateAiMarketReport(
        @PathVariable Long analysisId
) {
    return ResponseEntity.ok(
            aiMarketReportService.generateReport(analysisId)
    );
}

    @Operation(
        summary = "List interpretations by analysis",
        description = """
                Returns interpretations for all country results
                within an analysis, ordered by rank.
                """
)
@GetMapping("/analysis/{analysisId}/interpretations")
public ResponseEntity<List<CountryAnalysisInterpretation>>
getInterpretationsByAnalysisId(
        @PathVariable Long analysisId
) {
    return ResponseEntity.ok(
            resultService.getInterpretationsByAnalysisId(analysisId)
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