package com.exportpilot.analysisresult.ai;

public record AiAnalysisAnswer(

        Long analysisId,

        String model,

        String question,

        String answer

) {
}