package com.exportpilot.economicindicator.entity;

import com.exportpilot.country.entity.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "economic_indicators",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_economic_indicator_country_type_year_source",
                        columnNames = {
                                "country_id",
                                "indicator_type",
                                "indicator_year",
                                "source"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_economic_indicator_country",
                        columnList = "country_id"
                ),
                @Index(
                        name = "idx_economic_indicator_type",
                        columnList = "indicator_type"
                ),
                @Index(
                        name = "idx_economic_indicator_year",
                        columnList = "indicator_year"
                ),
                @Index(
                        name = "idx_economic_indicator_country_type",
                        columnList = "country_id, indicator_type"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "country_id",
            nullable = false
    )
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "indicator_type",
            nullable = false,
            length = 50
    )
    private EconomicIndicatorType indicatorType;

    @Column(
            name = "indicator_year",
            nullable = false
    )
    private Integer year;

    @Column(
            name = "indicator_value",
            precision = 24,
            scale = 8
    )
    private BigDecimal value;

    @Column(
            name = "unit",
            nullable = false,
            length = 100
    )
    private String unit;

    @Column(
            name = "source",
            nullable = false,
            length = 100
    )
    private String source;

    @Column(
            name = "source_indicator_code",
            nullable = false,
            length = 50
    )
    private String sourceIndicatorCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "data_status",
            nullable = false,
            length = 30
    )
    private EconomicDataStatus dataStatus;

    @Column(
            name = "source_updated_at"
    )
    private OffsetDateTime sourceUpdatedAt;

    @Column(
            name = "retrieved_at",
            nullable = false
    )
    private OffsetDateTime retrievedAt;

    @Builder.Default
    @Column(
            name = "is_latest",
            nullable = false
    )
    private Boolean latest = false;

    @Builder.Default
    @Column(
            name = "transformation_version",
            nullable = false,
            length = 30
    )
    private String transformationVersion = "v1";

    @PrePersist
    void onCreate() {
        if (retrievedAt == null) {
            retrievedAt = OffsetDateTime.now();
        }

        if (dataStatus == null) {
            dataStatus = value == null
                    ? EconomicDataStatus.MISSING
                    : EconomicDataStatus.AVAILABLE;
        }

        if (latest == null) {
            latest = false;
        }

        if (
                transformationVersion == null
                        || transformationVersion.isBlank()
        ) {
            transformationVersion = "v1";
        }

        if (
                unit == null
                        || unit.isBlank()
        ) {
            unit = indicatorType.getDefaultUnit();
        }

        if (
                sourceIndicatorCode == null
                        || sourceIndicatorCode.isBlank()
        ) {
            sourceIndicatorCode =
                    indicatorType.getWorldBankCode();
        }
    }
}