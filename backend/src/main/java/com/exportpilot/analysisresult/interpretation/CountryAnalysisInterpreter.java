package com.exportpilot.analysisresult.interpretation;

import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CountryAnalysisInterpreter {

    private static final BigDecimal VERY_HIGH_SCORE =
            new BigDecimal("80");

    private static final BigDecimal HIGH_SCORE =
            new BigDecimal("70");

    private static final BigDecimal MEDIUM_SCORE =
            new BigDecimal("40");

    private static final BigDecimal HIGH_HHI =
            new BigDecimal("2500");

    public CountryAnalysisInterpretation interpret(
            AnalysisCountryResult result
    ) {
        if (result == null) {
            throw new IllegalArgumentException(
                    "Analysis country result must not be null."
            );
        }

        String countryName = getCountryName(result);

        List<String> strengths = buildStrengths(result);
        List<String> risks = buildRisks(result);

        String summary = buildSummary(
                result,
                countryName
        );

        String decision = buildDecision(
                result,
                countryName
        );

        return new CountryAnalysisInterpretation(
                result.getId(),
                countryName,
                result.getRankPosition(),
                result.getOverallScore(),
                CountryRecommendationLevel.fromScore(
                        result.getOverallScore()
                ),
                summary,
                List.copyOf(strengths),
                List.copyOf(risks),
                decision
        );
    }

    private List<String> buildStrengths(
            AnalysisCountryResult result
    ) {
        List<String> strengths = new ArrayList<>();

        addImportMarketSizeStrength(
                result.getImportMarketSizeScore(),
                strengths
        );

        addImportGrowthStrength(
                result.getImportGrowthScore(),
                strengths
        );

        addTurkeyExportPerformanceStrength(
                result.getTurkeyExportPerformanceScore(),
                strengths
        );

        addMarketShareOpportunityStrength(
                result.getMarketShareOpportunityScore(),
                strengths
        );

        addCompetitiveAccessibilityStrength(
                result.getCompetitiveAccessibilityScore(),
                strengths
        );

        addMacroeconomicStrength(
                result.getMacroeconomicStabilityScore(),
                strengths
        );

        addCurrencyStrength(
                result.getCurrencyStabilityScore(),
                strengths
        );

        addLogisticsStrength(
                result.getLogisticsSuitabilityScore(),
                strengths
        );

        addTariffStrength(
                result.getTariffSuitabilityScore(),
                strengths
        );

        return strengths;
    }

    private List<String> buildRisks(
            AnalysisCountryResult result
    ) {
        List<String> risks = new ArrayList<>();

        addImportMarketSizeRisk(
                result.getImportMarketSizeScore(),
                risks
        );

        addImportGrowthRisk(
                result.getImportGrowthScore(),
                risks
        );

        addTurkeyExportPerformanceRisk(
                result.getTurkeyExportPerformanceScore(),
                risks
        );

        addCompetitiveAccessibilityRisk(
                result.getCompetitiveAccessibilityScore(),
                risks
        );

        addMacroeconomicRisk(
                result.getMacroeconomicStabilityScore(),
                risks
        );

        addCurrencyRisk(
                result.getCurrencyStabilityScore(),
                risks
        );

        addLogisticsRisk(
                result.getLogisticsSuitabilityScore(),
                risks
        );

        addTariffRisk(
                result.getTariffSuitabilityScore(),
                risks
        );

        addSupplierConcentrationRisk(
                result.getSupplierConcentrationHhi(),
                risks
        );

        addDataCompletenessRisk(
                result.getDataCompleteness(),
                risks
        );

        return risks;
    }

    private void addImportMarketSizeStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, VERY_HIGH_SCORE)) {
            strengths.add("İthalat pazari çok büyük");
        } else if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add("İthalat pazari büyük");
        }
    }

    private void addImportMarketSizeRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add("İthalat pazari büyüklüğü sinirli");
        }
    }

    private void addImportGrowthStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, VERY_HIGH_SCORE)) {
            strengths.add("İthalat talebi çok hizli büyüyor");
        } else if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add("İthalat talebi güçlü büyüyor");
        }
    }

    private void addImportGrowthRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add("İthalat büyümesi zayif");
        }
    }

    private void addTurkeyExportPerformanceStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, VERY_HIGH_SCORE)) {
            strengths.add(
                    "Türkiye'nin ihracat performansi çok güçlü"
            );
        } else if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add(
                    "Türkiye'nin ihracat performansi güçlü"
            );
        }
    }

    private void addTurkeyExportPerformanceRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add(
                    "Türkiye'nin ihracat performansi zayif"
            );
        }
    }

    private void addMarketShareOpportunityStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add(
                    "Türkiye için pazar payini artirma firsati yüksek"
            );
        }
    }

    private void addCompetitiveAccessibilityStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add(
                    "Pazara rekabet açisindan erişim uygun"
            );
        }
    }

    private void addCompetitiveAccessibilityRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add("Rekabet baskisi yüksek");
        }
    }

    private void addMacroeconomicStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add(
                    "Makroekonomik ortam istikrarli"
            );
        }
    }

    private void addMacroeconomicRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add(
                    "Makroekonomik istikrar zayif"
            );
        }
    }

    private void addCurrencyStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add("Kur koşullari istikrarli");
        }
    }

    private void addCurrencyRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add(
                    "Kur oynakliği önemli risk oluşturuyor"
            );
        }
    }

    private void addLogisticsStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, HIGH_SCORE)) {
            strengths.add("Lojistik koşullar uygun");
        }
    }

    private void addLogisticsRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, MEDIUM_SCORE)) {
            risks.add("Lojistik uygunluk zayif");
        }
    }

    private void addTariffStrength(
            BigDecimal score,
            List<String> strengths
    ) {
        if (isAtLeast(score, VERY_HIGH_SCORE)) {
            strengths.add(
                    "Tarife ve pazar erişim koşullari avantajli"
            );
        } else if (isAtLeast(score, new BigDecimal("60"))) {
            strengths.add(
                    "Tarife koşullari genel olarak uygun"
            );
        }
    }

    private void addTariffRisk(
            BigDecimal score,
            List<String> risks
    ) {
        if (isBelow(score, new BigDecimal("60"))) {
            risks.add(
                    "Tarife veya pazar erişimi risk oluşturabilir"
            );
        }
    }

    private void addSupplierConcentrationRisk(
            BigDecimal hhi,
            List<String> risks
    ) {
        if (hhi != null && hhi.compareTo(HIGH_HHI) >= 0) {
            risks.add(
                    "Pazar tedarikçi yoğunlaşmasi yüksek"
            );
        }
    }

    private void addDataCompletenessRisk(
            BigDecimal dataCompleteness,
            List<String> risks
    ) {
        if (dataCompleteness == null) {
            risks.add(
                    "Veri yeterliliği hakkinda doğrulanmiş bilgi bulunmuyor"
            );
            return;
        }

        if (dataCompleteness.compareTo(
                new BigDecimal("80")
        ) < 0) {
            risks.add(
                    "Analiz verilerinde eksiklik bulunuyor"
            );
        }
    }

    private String buildSummary(
            AnalysisCountryResult result,
            String countryName
    ) {
        String primaryStrength =
                determinePrimaryStrength(result);

        String primaryRisk =
                determinePrimaryRisk(result);

        if (primaryRisk == null) {
            return countryName
                    + ", "
                    + primaryStrength
                    + " sayesinde güçlü bir hedef pazar görünümü sunmaktadir.";
        }

        return countryName
                + ", "
                + primaryStrength
                + " sayesinde dikkat çekici bir hedef pazar görünümü sunmaktadir. "
                + "Ancak "
                + primaryRisk
                + " karar sürecinde dikkatle değerlendirilmelidir.";
    }

    private String determinePrimaryStrength(
            AnalysisCountryResult result
    ) {
        BigDecimal highestScore = null;
        String strength =
                "mevcut göstergelerde dengeli bir performans göstermesi";

        highestScore = selectHigherScore(
                result.getImportMarketSizeScore(),
                highestScore
        );

        if (sameScore(
                highestScore,
                result.getImportMarketSizeScore()
        )) {
            strength = "ithalat pazarinin büyüklüğü";
        }

        if (isHigher(
                result.getImportGrowthScore(),
                highestScore
        )) {
            highestScore = result.getImportGrowthScore();
            strength = "güçlü ithalat büyümesi";
        }

        if (isHigher(
                result.getTurkeyExportPerformanceScore(),
                highestScore
        )) {
            highestScore =
                    result.getTurkeyExportPerformanceScore();
            strength =
                    "Türkiye'nin güçlü ihracat performansi";
        }

        if (isHigher(
                result.getMarketShareOpportunityScore(),
                highestScore
        )) {
            highestScore =
                    result.getMarketShareOpportunityScore();
            strength =
                    "yüksek pazar payi firsati";
        }

        if (isHigher(
                result.getCompetitiveAccessibilityScore(),
                highestScore
        )) {
            highestScore =
                    result.getCompetitiveAccessibilityScore();
            strength =
                    "uygun rekabet koşullari";
        }

        if (isHigher(
                result.getMacroeconomicStabilityScore(),
                highestScore
        )) {
            highestScore =
                    result.getMacroeconomicStabilityScore();
            strength =
                    "istikrarli makroekonomik ortami";
        }

        if (isHigher(
                result.getCurrencyStabilityScore(),
                highestScore
        )) {
            highestScore =
                    result.getCurrencyStabilityScore();
            strength = "istikrarli kur koşullari";
        }

        if (isHigher(
                result.getLogisticsSuitabilityScore(),
                highestScore
        )) {
            highestScore =
                    result.getLogisticsSuitabilityScore();
            strength = "uygun lojistik koşullari";
        }

        if (isHigher(
                result.getTariffSuitabilityScore(),
                highestScore
        )) {
            strength =
                    "avantajli tarife ve pazar erişim koşullari";
        }

        return strength;
    }

    private String determinePrimaryRisk(
            AnalysisCountryResult result
    ) {
        BigDecimal lowestScore = null;
        String risk = null;

        lowestScore = selectLowerScore(
                result.getImportMarketSizeScore(),
                lowestScore
        );

        if (result.getImportMarketSizeScore() != null) {
            risk = "sinirli ithalat pazari büyüklüğü";
        }

        if (isLower(
                result.getImportGrowthScore(),
                lowestScore
        )) {
            lowestScore = result.getImportGrowthScore();
            risk = "zayif ithalat büyümesi";
        }

        if (isLower(
                result.getTurkeyExportPerformanceScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getTurkeyExportPerformanceScore();
            risk =
                    "Türkiye'nin düşük ihracat performansi";
        }

        if (isLower(
                result.getMarketShareOpportunityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getMarketShareOpportunityScore();
            risk =
                    "sinirli pazar payi firsati";
        }

        if (isLower(
                result.getCompetitiveAccessibilityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getCompetitiveAccessibilityScore();
            risk = "yüksek rekabet baskisi";
        }

        if (isLower(
                result.getMacroeconomicStabilityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getMacroeconomicStabilityScore();
            risk =
                    "zayif makroekonomik istikrar";
        }

        if (isLower(
                result.getCurrencyStabilityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getCurrencyStabilityScore();
            risk = "yüksek kur riski";
        }

        if (isLower(
                result.getLogisticsSuitabilityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getLogisticsSuitabilityScore();
            risk = "zayif lojistik uygunluk";
        }

        if (isLower(
                result.getTariffSuitabilityScore(),
                lowestScore
        )) {
            lowestScore =
                    result.getTariffSuitabilityScore();
            risk =
                    "tarife ve pazar erişim riski";
        }

        if (lowestScore == null
                || lowestScore.compareTo(MEDIUM_SCORE) >= 0) {
            return null;
        }

        return risk;
    }

    private String buildDecision(
            AnalysisCountryResult result,
            String countryName
    ) {
        CountryRecommendationLevel level =
                CountryRecommendationLevel.fromScore(
                        result.getOverallScore()
                );

        return switch (level) {
            case VERY_HIGH ->
                    "Seçilen göstergeler ve tarihsel eğilimler temelinde "
                            + countryName
                            + ", şu anda en yüksek pazar potansiyelini gösteren ülkelerden biridir.";

            case HIGH ->
                    "Seçilen göstergeler ve tarihsel eğilimler temelinde "
                            + countryName
                            + ", öncelikli olarak incelenebilecek hedef pazarlardan biridir.";

            case MEDIUM ->
                    countryName
                            + ", belirli firsatlar sunmakla birlikte riskleri ve zayif göstergeleri dikkate alinarak değerlendirilmelidir.";

            case LOW ->
                    countryName
                            + ", mevcut göstergelere göre sinirli pazar potansiyeli göstermektedir.";

            case VERY_LOW ->
                    countryName
                            + ", mevcut veriler temelinde öncelikli hedef pazar olarak değerlendirilmemektedir.";
        };
    }

    private String getCountryName(
            AnalysisCountryResult result
    ) {
        if (result.getCountry() == null
                || result.getCountry().getName() == null
                || result.getCountry().getName().isBlank()) {
            return "Seçilen ülke";
        }

        return result.getCountry().getName();
    }

    private boolean isAtLeast(
            BigDecimal value,
            BigDecimal threshold
    ) {
        return value != null
                && value.compareTo(threshold) >= 0;
    }

    private boolean isBelow(
            BigDecimal value,
            BigDecimal threshold
    ) {
        return value != null
                && value.compareTo(threshold) < 0;
    }

    private boolean isHigher(
            BigDecimal candidate,
            BigDecimal current
    ) {
        return candidate != null
                && (
                current == null
                        || candidate.compareTo(current) > 0
        );
    }

    private boolean isLower(
            BigDecimal candidate,
            BigDecimal current
    ) {
        return candidate != null
                && (
                current == null
                        || candidate.compareTo(current) < 0
        );
    }

    private BigDecimal selectHigherScore(
            BigDecimal candidate,
            BigDecimal current
    ) {
        if (isHigher(candidate, current)) {
            return candidate;
        }

        return current;
    }

    private BigDecimal selectLowerScore(
            BigDecimal candidate,
            BigDecimal current
    ) {
        if (isLower(candidate, current)) {
            return candidate;
        }

        return current;
    }

    private boolean sameScore(
            BigDecimal first,
            BigDecimal second
    ) {
        return first != null
                && second != null
                && first.compareTo(second) == 0;
    }
}