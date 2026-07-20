package com.exportpilot.analysisresult.entity;

import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.country.entity.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "analysis_country_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_analysis_country",
                        columnNames = {
                                "analysis_id",
                                "country_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisCountryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "analysis_id",
            nullable = false
    )
    private Analysis analysis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "country_id",
            nullable = false
    )
    private Country country;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(
            name = "overall_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal overallScore;

    @Column(
            name = "import_market_size_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal importMarketSizeScore;

    @Column(
            name = "import_growth_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal importGrowthScore;

    @Column(
            name = "turkey_export_performance_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal turkeyExportPerformanceScore;

    @Column(
            name = "market_share_opportunity_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal marketShareOpportunityScore;

    @Column(
            name = "competitive_accessibility_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal competitiveAccessibilityScore;

    @Column(name = "supplier_count")
    private Integer supplierCount;

    @Column(
            name = "supplier_concentration_hhi",
            precision = 12,
            scale = 2
    )
    private BigDecimal supplierConcentrationHhi;

    @Column(name = "turkey_supplier_rank")
    private Integer turkeySupplierRank;

    @Column(
            name = "turkey_market_share_percent",
            precision = 12,
            scale = 4
    )
    private BigDecimal turkeyMarketSharePercent;

    @Column(
            name = "leader_market_share_percent",
            precision = 12,
            scale = 4
    )
    private BigDecimal leaderMarketSharePercent;

    @Column(
            name = "distance_to_leader_percent",
            precision = 12,
            scale = 4
    )
    private BigDecimal distanceToLeaderPercent;

    @Column(
            name = "turkey_unit_value_usd_per_kg",
            precision = 20,
            scale = 4
    )
    private BigDecimal turkeyUnitValueUsdPerKg;

    @Column(
            name = "market_average_unit_value_usd_per_kg",
            precision = 20,
            scale = 4
    )
    private BigDecimal marketAverageUnitValueUsdPerKg;

    @Column(
            name = "macroeconomic_stability_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal macroeconomicStabilityScore;

    @Column(
            name = "currency_stability_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal currencyStabilityScore;

    @Column(
            name = "logistics_suitability_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal logisticsSuitabilityScore;

    @Column(
            name = "tariff_suitability_score",
            precision = 5,
            scale = 2
    )
    private BigDecimal tariffSuitabilityScore;

    @Column(
            name = "data_completeness",
            precision = 5,
            scale = 2
    )
    private BigDecimal dataCompleteness;

    @Column(name = "first_year")
    private Integer firstYear;

    @Column(name = "last_year")
    private Integer lastYear;

    @Column(name = "available_year_count")
    private Integer availableYearCount;

    @Column(
            name = "first_year_trade_value_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal firstYearTradeValueUsd;

    @Column(
            name = "last_year_trade_value_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal lastYearTradeValueUsd;

    @Column(
            name = "total_trade_value_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal totalTradeValueUsd;

    @Column(
            name = "average_trade_value_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal averageTradeValueUsd;

    @Column(
            name = "absolute_growth_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal absoluteGrowthUsd;

    @Column(
            name = "growth_rate_percent",
            precision = 12,
            scale = 4
    )
    private BigDecimal growthRatePercent;

    @Column(
            name = "cagr_percent",
            precision = 12,
            scale = 4
    )
    private BigDecimal cagrPercent;

    @Column(name = "calculated_at")
    private OffsetDateTime calculatedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (calculatedAt == null) {
            calculatedAt = now;
        }
    }
}