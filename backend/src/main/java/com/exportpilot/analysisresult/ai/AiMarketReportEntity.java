package com.exportpilot.analysisresult.ai;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_market_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_market_reports_analysis_id",
                        columnNames = "analysis_id"
                )
        }
)
public class AiMarketReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analysis_id", nullable = false, unique = true)
    private Long analysisId;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String report;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AiMarketReportEntity() {
    }

    public AiMarketReportEntity(
            Long analysisId,
            String model,
            String report
    ) {
        this.analysisId = analysisId;
        this.model = model;
        this.report = report;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getAnalysisId() {
        return analysisId;
    }

    public String getModel() {
        return model;
    }

    public String getReport() {
        return report;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateReport(
            String model,
            String report
    ) {
        this.model = model;
        this.report = report;
    }
}
