package com.exportpilot.analysisresult.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiAnalysisQuestionRequest(

        @NotBlank(message = "Soru bos olamaz.")
        @Size(
                max = 500,
                message = "Soru en fazla 500 karakter olabilir."
        )
        String question

) {
}