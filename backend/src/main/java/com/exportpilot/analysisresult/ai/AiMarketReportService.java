package com.exportpilot.analysisresult.ai;

import com.exportpilot.analysisresult.interpretation.CountryAnalysisInterpretation;
import com.exportpilot.analysisresult.service.AnalysisCountryResultService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiMarketReportService {

    private final AnalysisCountryResultService resultService;
    private final GeminiMarketReportClient geminiClient;

    public AiMarketReportService(
            AnalysisCountryResultService resultService,
            GeminiMarketReportClient geminiClient
    ) {
        this.resultService = resultService;
        this.geminiClient = geminiClient;
    }

    public AiMarketReport generateReport(Long analysisId) {
        List<CountryAnalysisInterpretation> interpretations =
                resultService.getInterpretationsByAnalysisId(analysisId);

        if (interpretations.isEmpty()) {
            throw new IllegalStateException(
                    "Bu analize ait yorumlanmis ulke sonucu bulunamadi."
            );
        }

        String prompt = buildPrompt(analysisId, interpretations);

        String report = geminiClient.generateReport(prompt);

        return new AiMarketReport(
                analysisId,
                geminiClient.getModel(),
                report
        );
    }

    private String buildPrompt(
            Long analysisId,
            List<CountryAnalysisInterpretation> interpretations
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Sen deneyimli bir dis ticaret ve ihracat pazari analistisin.

                Asagidaki ulke analiz sonuclarini inceleyerek Turkce bir pazar
                degerlendirme raporu hazirla.

                Rapor su bolumlerden olussun:

                1. Genel Degerlendirme
                2. En Uygun Pazarlar
                3. Ulke Bazli Firsatlar
                4. Temel Riskler
                5. Sonuc ve Oneri

                Kurallar:
                - Yalnizca verilen verilere dayan.
                - Verilmeyen sayisal bilgileri uydurma.
                - Ulkeleri genel skor ve siralamalarina gore karsilastir.
                - Riskleri ve guclu yonleri birlikte degerlendir.
                - Profesyonel fakat anlasilir bir dil kullan.
                - Raporu Turkce yaz.
                - Yaklasik 400-600 kelime kullan.

                """);

        prompt.append("Analiz ID: ")
                .append(analysisId)
                .append("\n\n");

        for (CountryAnalysisInterpretation interpretation : interpretations) {
            prompt.append("Ulke: ")
                    .append(interpretation.countryName())
                    .append("\n");

            prompt.append("Siralama: ")
                    .append(interpretation.rankPosition())
                    .append("\n");

            prompt.append("Genel skor: ")
                    .append(interpretation.overallScore())
                    .append("\n");

            prompt.append("Oneri seviyesi: ")
                    .append(interpretation.recommendationLevel())
                    .append("\n");

            prompt.append("Ozet: ")
                    .append(interpretation.summary())
                    .append("\n");

            prompt.append("Guclu yonler: ")
                    .append(String.join(", ", interpretation.strengths()))
                    .append("\n");

            prompt.append("Riskler: ")
                    .append(String.join(", ", interpretation.risks()))
                    .append("\n");

            prompt.append("Karar: ")
                    .append(interpretation.decision())
                    .append("\n");

            prompt.append("------------------------------\n");
        }

        return prompt.toString();
    }
}