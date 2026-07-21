package com.exportpilot.analysisresult.controller;

import com.exportpilot.analysisresult.ai.AiAnalysisAnswer;
import com.exportpilot.analysisresult.ai.AiAnalysisChatService;
import com.exportpilot.analysisresult.ai.AiAnalysisQuestionRequest;
import com.exportpilot.analysisresult.ai.AiMarketReport;
import com.exportpilot.analysisresult.ai.AiMarketReportPdfService;
import com.exportpilot.analysisresult.ai.AiMarketReportService;
import com.exportpilot.analysisresult.dto.AnalysisCountryResultResponse;
import com.exportpilot.analysisresult.interpretation.CountryAnalysisInterpretation;
import com.exportpilot.analysisresult.service.AnalysisCountryResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final AiMarketReportService aiMarketReportService;
    private final AiMarketReportPdfService aiMarketReportPdfService;
    private final AiAnalysisChatService aiAnalysisChatService;

    public AnalysisCountryResultController(
            AnalysisCountryResultService resultService,
            AiMarketReportService aiMarketReportService,
            AiMarketReportPdfService aiMarketReportPdfService,
            AiAnalysisChatService aiAnalysisChatService
    ) {
        this.resultService = resultService;
        this.aiMarketReportService = aiMarketReportService;
        this.aiMarketReportPdfService = aiMarketReportPdfService;
        this.aiAnalysisChatService = aiAnalysisChatService;
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
            summary = "Generate or get AI market report",
            description = """
                    Returns the stored AI market report for an analysis.
                    If no report exists, generates one with Gemini and stores it.
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
            summary = "Regenerate AI market report",
            description = """
                    Generates a new AI market report with Gemini
                    and updates the stored report for the analysis.
                    """
    )
    @PostMapping("/analysis/{analysisId}/ai-report/regenerate")
    public ResponseEntity<AiMarketReport> regenerateAiMarketReport(
            @PathVariable Long analysisId
    ) {
        return ResponseEntity.ok(
                aiMarketReportService.regenerateReport(analysisId)
        );
    }

    @Operation(
            summary = "Ask a question about an analysis",
            description = """
                    Sends a user question together with the deterministic
                    country analysis results to Gemini and returns an
                    AI-supported answer based only on the available data.
                    """
    )
    @PostMapping("/analysis/{analysisId}/ai-chat")
    public ResponseEntity<AiAnalysisAnswer> askAnalysisQuestion(
            @PathVariable Long analysisId,
            @Valid @RequestBody AiAnalysisQuestionRequest request
    ) {
        return ResponseEntity.ok(
                aiAnalysisChatService.askQuestion(
                        analysisId,
                        request.question()
                )
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

    @Operation(
            summary = "Download AI market report as PDF",
            description = """
                    Creates a PDF document from the stored AI market report
                    and returns it as a downloadable file.
                    """
    )
    @GetMapping("/analysis/{analysisId}/ai-report/pdf")
    public ResponseEntity<?> downloadAiMarketReportPdf(
            @PathVariable Long analysisId
    ) {
        try {
            byte[] pdf =
                    aiMarketReportPdfService.generatePdf(analysisId);

            String fileName =
                    "exportpilot-analysis-"
                            + analysisId
                            + "-report.pdf";

            ContentDisposition contentDisposition =
                    ContentDisposition
                            .attachment()
                            .filename(fileName)
                            .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            contentDisposition.toString()
                    )
                    .contentLength(pdf.length)
                    .body(pdf);

        } catch (Exception exception) {
            exception.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(
                            "PDF oluşturulamadı: "
                                    + exception.getMessage()
                    );
        }
    }
}