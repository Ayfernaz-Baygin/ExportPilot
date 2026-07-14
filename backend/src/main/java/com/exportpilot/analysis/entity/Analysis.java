package com.exportpilot.analysis.entity;

import com.exportpilot.product.entity.Product;
import com.exportpilot.productcode.entity.ProductCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_code_id", nullable = false)
    private ProductCode productCode;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "end_year", nullable = false)
    private Integer endYear;

    @Column(name = "target_region", length = 100)
    private String targetRegion;

    @Builder.Default
    @Column(name = "max_countries", nullable = false)
    private Integer maxCountries = 20;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Builder.Default
    @Column(
            name = "scoring_model_version",
            nullable = false,
            length = 50
    )
    private String scoringModelVersion = "v1";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (status == null) {
            status = AnalysisStatus.PENDING;
        }

        if (maxCountries == null) {
            maxCountries = 20;
        }

        if (scoringModelVersion == null || scoringModelVersion.isBlank()) {
            scoringModelVersion = "v1";
        }
    }
}