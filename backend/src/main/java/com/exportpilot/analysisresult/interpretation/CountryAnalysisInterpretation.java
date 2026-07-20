package com.exportpilot.analysisresult.interpretation;

import java.math.BigDecimal;
import java.util.List;

public record CountryAnalysisInterpretation(

        Long analysisResultId,

        String countryName,

        Integer rankPosition,

        BigDecimal overallScore,

        CountryRecommendationLevel recommendationLevel,

        String summary,

        List<String> strengths,

        List<String> risks,

        String decision

) {
}