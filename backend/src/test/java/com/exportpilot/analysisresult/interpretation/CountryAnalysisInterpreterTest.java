package com.exportpilot.analysisresult.interpretation;

import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import com.exportpilot.country.entity.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountryAnalysisInterpreterTest {

    private CountryAnalysisInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new CountryAnalysisInterpreter();
    }

    @Test
    void shouldInterpretRomaniaAsHighPriorityMarket() {
        Country country = mock(Country.class);

        when(country.getName()).thenReturn("Romania");

        AnalysisCountryResult result = AnalysisCountryResult.builder()
                .id(61L)
                .country(country)
                .rankPosition(1)
                .overallScore(new BigDecimal("64.52"))
                .importMarketSizeScore(new BigDecimal("32.66"))
                .importGrowthScore(new BigDecimal("100.00"))
                .turkeyExportPerformanceScore(
                        new BigDecimal("100.00")
                )
                .marketShareOpportunityScore(
                        new BigDecimal("54.80")
                )
                .competitiveAccessibilityScore(
                        new BigDecimal("36.42")
                )
                .macroeconomicStabilityScore(
                        new BigDecimal("7.48")
                )
                .currencyStabilityScore(
                        new BigDecimal("85.00")
                )
                .logisticsSuitabilityScore(
                        new BigDecimal("83.45")
                )
                .tariffSuitabilityScore(
                        new BigDecimal("94.00")
                )
                .supplierConcentrationHhi(
                        new BigDecimal("4966.50")
                )
                .dataCompleteness(
                        new BigDecimal("100.00")
                )
                .build();

        CountryAnalysisInterpretation interpretation =
                interpreter.interpret(result);

        assertThat(interpretation.analysisResultId())
                .isEqualTo(61L);

        assertThat(interpretation.countryName())
                .isEqualTo("Romania");

        assertThat(interpretation.rankPosition())
                .isEqualTo(1);

        assertThat(interpretation.overallScore())
                .isEqualByComparingTo("64.52");

        assertThat(interpretation.recommendationLevel())
                .isEqualTo(CountryRecommendationLevel.HIGH);

        assertThat(interpretation.strengths())
                .contains(
                        "İthalat talebi çok hizli büyüyor",
                        "Türkiye'nin ihracat performansi çok güçlü",
                        "Kur koşullari istikrarli",
                        "Lojistik koşullar uygun",
                        "Tarife ve pazar erişim koşullari avantajli"
                );

        assertThat(interpretation.risks())
                .contains(
                        "İthalat pazari büyüklüğü sinirli",
                        "Rekabet baskisi yüksek",
                        "Makroekonomik istikrar zayif",
                        "Pazar tedarikçi yoğunlaşmasi yüksek"
                );

        assertThat(interpretation.summary())
                .contains("Romania")
                .contains("güçlü ithalat büyümesi")
                .contains("zayif makroekonomik istikrar");

        assertThat(interpretation.decision())
                .contains(
                        "öncelikli olarak incelenebilecek hedef pazarlardan biridir"
                );
    }

    @Test
    void shouldInterpretGermanyAsMediumPriorityMarket() {
        Country country = mock(Country.class);

        when(country.getName()).thenReturn("Germany");

        AnalysisCountryResult result = AnalysisCountryResult.builder()
                .id(63L)
                .country(country)
                .rankPosition(3)
                .overallScore(new BigDecimal("51.35"))
                .importMarketSizeScore(new BigDecimal("100.00"))
                .importGrowthScore(new BigDecimal("0.00"))
                .turkeyExportPerformanceScore(
                        new BigDecimal("31.14")
                )
                .marketShareOpportunityScore(
                        new BigDecimal("40.27")
                )
                .competitiveAccessibilityScore(
                        new BigDecimal("36.80")
                )
                .macroeconomicStabilityScore(
                        new BigDecimal("57.45")
                )
                .currencyStabilityScore(
                        new BigDecimal("88.15")
                )
                .logisticsSuitabilityScore(
                        new BigDecimal("81.20")
                )
                .tariffSuitabilityScore(
                        new BigDecimal("95.20")
                )
                .supplierConcentrationHhi(
                        new BigDecimal("4790.91")
                )
                .dataCompleteness(
                        new BigDecimal("100.00")
                )
                .build();

        CountryAnalysisInterpretation interpretation =
                interpreter.interpret(result);

        assertThat(interpretation.recommendationLevel())
                .isEqualTo(CountryRecommendationLevel.MEDIUM);

        assertThat(interpretation.strengths())
                .contains(
                        "İthalat pazari çok büyük",
                        "Kur koşullari istikrarli",
                        "Lojistik koşullar uygun",
                        "Tarife ve pazar erişim koşullari avantajli"
                );

        assertThat(interpretation.risks())
                .contains(
                        "İthalat büyümesi zayif",
                        "Türkiye'nin ihracat performansi zayif",
                        "Rekabet baskisi yüksek",
                        "Pazar tedarikçi yoğunlaşmasi yüksek"
                );

        assertThat(interpretation.summary())
                .contains("Germany")
                .contains("ithalat pazarinin büyüklüğü")
                .contains("zayif ithalat büyümesi");

        assertThat(interpretation.decision())
                .contains(
                        "belirli firsatlar sunmakla birlikte"
                );
    }

    @Test
    void shouldNotTreatMissingScoreAsZero() {
        Country country = mock(Country.class);

        when(country.getName()).thenReturn("Test Country");

        AnalysisCountryResult result = AnalysisCountryResult.builder()
                .id(100L)
                .country(country)
                .overallScore(new BigDecimal("50.00"))
                .dataCompleteness(null)
                .build();

        CountryAnalysisInterpretation interpretation =
                interpreter.interpret(result);

        assertThat(interpretation.risks())
                .contains(
                        "Veri yeterliliği hakkinda doğrulanmiş bilgi bulunmuyor"
                );

        assertThat(interpretation.risks())
                .doesNotContain(
                        "İthalat büyümesi zayif",
                        "Türkiye'nin ihracat performansi zayif",
                        "Kur oynakliği önemli risk oluşturuyor"
                );
    }
}