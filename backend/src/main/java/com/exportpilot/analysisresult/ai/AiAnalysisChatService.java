package com.exportpilot.analysisresult.ai;

import com.exportpilot.analysisresult.interpretation.CountryAnalysisInterpretation;
import com.exportpilot.analysisresult.service.AnalysisCountryResultService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAnalysisChatService {

    private static final String OUT_OF_SCOPE_RESPONSE =
            "Bu asistan yalnızca mevcut ihracat analizi sonuçlarını "
                    + "açıklamak, ülkeleri karşılaştırmak ve pazara giriş "
                    + "stratejileri oluşturmak için kullanılabilir.";

    private final AnalysisCountryResultService resultService;
    private final GeminiMarketReportClient geminiClient;

    public AiAnalysisChatService(
            AnalysisCountryResultService resultService,
            GeminiMarketReportClient geminiClient
    ) {
        this.resultService = resultService;
        this.geminiClient = geminiClient;
    }

    public AiAnalysisAnswer askQuestion(
            Long analysisId,
            String question
    ) {
        validateQuestion(question);

        List<CountryAnalysisInterpretation> interpretations =
                resultService.getInterpretationsByAnalysisId(analysisId);

        if (interpretations.isEmpty()) {
            throw new IllegalStateException(
                    "Bu analize ait yorumlanmış ülke sonucu bulunamadı."
            );
        }

        String normalizedQuestion = question.trim();

        if (isClearlyOutOfScope(normalizedQuestion)) {
            return new AiAnalysisAnswer(
                    analysisId,
                    geminiClient.getModel(),
                    normalizedQuestion,
                    OUT_OF_SCOPE_RESPONSE
            );
        }

        String prompt = buildPrompt(
                analysisId,
                normalizedQuestion,
                interpretations
        );

        String answer = geminiClient.generateChatAnswer(prompt);

        return new AiAnalysisAnswer(
                analysisId,
                geminiClient.getModel(),
                normalizedQuestion,
                answer
        );
    }

    private void validateQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Soru boş olamaz."
            );
        }

        if (question.length() > 500) {
            throw new IllegalArgumentException(
                    "Soru en fazla 500 karakter olabilir."
            );
        }
    }

    private boolean isClearlyOutOfScope(String question) {
        String normalized = question
                .toLowerCase()
                .replace("?", "")
                .replace("!", "")
                .trim();

        return normalized.equals("nasılsın")
                || normalized.equals("nasilsin")
                || normalized.equals("iyi misin")
                || normalized.equals("iyisin umarım")
                || normalized.equals("kaç yaşındasın")
                || normalized.equals("kac yasindasin")
                || normalized.equals("sen kimsin")
                || normalized.equals("adın ne")
                || normalized.equals("adin ne")
                || normalized.equals("ne yapıyorsun")
                || normalized.equals("ne yapiyorsun");
    }

    private String buildPrompt(
            Long analysisId,
            String question,
            List<CountryAnalysisInterpretation> interpretations
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                Sen ExportPilot uygulamasında çalışan deneyimli bir
                dış ticaret, ihracat pazarı ve pazara giriş stratejisi danışmanısın.

                Kullanıcı, daha önce hesaplanmış ülke analiz sonuçları
                hakkında soru soracak veya ihracat stratejisi isteyecek.

                Görevlerin:
                - Analiz sonuçlarını açıklamak.
                - Ülkeleri karşılaştırmak.
                - Bir ülkenin neden önerildiğini açıklamak.
                - Güçlü yönleri ve riskleri değerlendirmek.
                - Uygun ülke için pazara giriş stratejisi oluşturmak.
                - Kullanıcı isterse uygulanabilir ilk adımları sıralamak.
                - Risk azaltma önerileri sunmak.
                - Kısa, orta ve uzun vadeli aksiyonlar önermek.

                Kesin kurallar:
                - Yalnızca aşağıda verilen analiz verilerine dayan.
                - Verilmeyen puanları, oranları veya istatistikleri uydurma.
                - Analizde yer almayan gümrük vergisi, lojistik maliyeti,
                  mevzuat veya ürün talebi bilgilerini kesin gerçek gibi sunma.
                - Eksik bilgi varsa bunu açıkça belirt.
                - Genel ihracat bilgisi verirsen bunun genel bir öneri olduğunu belirt.
                - Kullanıcı belirli bir ülke sorarsa yalnızca o ülkeye odaklan.
                - Kullanıcı karşılaştırma isterse ilgili ülkeleri açık kriterlerle karşılaştır.
                - Kullanıcı strateji isterse cevabı uygulanabilir adımlara böl.
                - Soruyla ilgisiz uzun bir rapor oluşturma.
                - Cevabı Türkçe yaz.
                - Açık, profesyonel ve doğrudan bir dil kullan.
                - Yaklaşık 120-300 kelime kullan.

                Strateji cevabı istenirse mümkün olduğunda şu yapıyı kullan:

                1. Stratejik değerlendirme
                2. Önerilen giriş yaklaşımı
                3. İlk uygulanabilir adımlar
                4. Temel riskler
                5. Risk azaltma önerileri

                Kullanıcı analiz, ihracat, ülke karşılaştırması veya pazara giriş
                ile ilgisiz bir soru sorarsa normal sohbet yapma.

                Böyle bir durumda yalnızca şu cevabı ver:

                "Bu asistan yalnızca mevcut ihracat analizi sonuçlarını
                açıklamak, ülkeleri karşılaştırmak ve pazara giriş
                stratejileri oluşturmak için kullanılabilir."

                """);

        prompt.append("Analiz ID: ")
                .append(analysisId)
                .append("\n\n");

        prompt.append("Kullanıcı sorusu:\n")
                .append(question)
                .append("\n\n");

        prompt.append("Analiz verileri:\n\n");

        for (CountryAnalysisInterpretation interpretation : interpretations) {
            appendInterpretation(prompt, interpretation);
        }

        prompt.append("""
                
                Son talimat:
                Kullanıcının sorusunu doğrudan cevapla.
                Analiz verilerinde bulunmayan bilgileri kesin bilgi gibi yazma.
                """);

        return prompt.toString();
    }

    private void appendInterpretation(
            StringBuilder prompt,
            CountryAnalysisInterpretation interpretation
    ) {
        prompt.append("Ülke: ")
                .append(interpretation.countryName())
                .append("\n");

        prompt.append("Sıralama: ")
                .append(interpretation.rankPosition())
                .append("\n");

        prompt.append("Genel skor: ")
                .append(interpretation.overallScore())
                .append("\n");

        prompt.append("Öneri seviyesi: ")
                .append(interpretation.recommendationLevel())
                .append("\n");

        prompt.append("Özet: ")
                .append(interpretation.summary())
                .append("\n");

        prompt.append("Güçlü yönler: ")
                .append(formatList(interpretation.strengths()))
                .append("\n");

        prompt.append("Riskler: ")
                .append(formatList(interpretation.risks()))
                .append("\n");

        prompt.append("Karar: ")
                .append(interpretation.decision())
                .append("\n");

        prompt.append("----------------------------------------\n");
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "Belirtilmemiş";
        }

        return String.join(", ", values);
    }
}