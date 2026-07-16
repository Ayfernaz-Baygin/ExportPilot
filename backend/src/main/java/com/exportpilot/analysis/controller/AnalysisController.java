package com.exportpilot.analysis.controller;

import com.exportpilot.analysis.dto.AnalysisResponse;
import com.exportpilot.analysis.dto.CreateAnalysisRequest;
import com.exportpilot.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.exportpilot.analysisresult.service.AnalysisCountryResultGenerator;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses")
@Tag(
        name = "Analyses",
        description = "Export market analysis operations"
)
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisCountryResultGenerator resultGenerator;

    public AnalysisController(
        AnalysisService analysisService,
        AnalysisCountryResultGenerator resultGenerator
) {
    this.analysisService = analysisService;
    this.resultGenerator = resultGenerator;
}

    @Operation(
            summary = "Create export analysis",
            description = "Creates a pending export market analysis."
    )
    @PostMapping
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @Valid @RequestBody CreateAnalysisRequest request
    ) {
        AnalysisResponse response =
                analysisService.createAnalysis(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "List analyses",
            description = "Returns analysis history ordered from newest to oldest."
    )
    @GetMapping
    public ResponseEntity<List<AnalysisResponse>> getAnalyses() {
        return ResponseEntity.ok(
                analysisService.getAnalyses()
        );
    }

    @Operation(
            summary = "Get analysis by ID",
            description = "Returns the requested analysis."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponse> getAnalysisById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                analysisService.getAnalysisById(id)
        );
    }

    @Operation(
        summary = "Generate analysis results",
        description = "Calculates and stores country-level results for an existing analysis."
)
@PostMapping("/{analysisId}/generate-results")
public ResponseEntity<Void> generateResults(
        @PathVariable Long analysisId
) {
    resultGenerator.generateResults(analysisId);

    return ResponseEntity.noContent().build();
}
}