package com.exportpilot.trade.entity;

import com.exportpilot.country.entity.Country;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trade_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "source",
            nullable = false,
            length = 50
    )
    private String source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reporter_country_id",
            nullable = false
    )
    private Country reporterCountry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "partner_country_id",
            nullable = false
    )
    private Country partnerCountry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_code_id",
            nullable = false
    )
    private ProductCode productCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "trade_flow",
            nullable = false,
            length = 10
    )
    private TradeFlow tradeFlow;

    @Column(
            name = "trade_year",
            nullable = false
    )
    private Integer tradeYear;

    @Column(
            name = "trade_value_usd",
            precision = 20,
            scale = 2
    )
    private BigDecimal tradeValueUsd;

    @Column(
            name = "quantity",
            precision = 20,
            scale = 3
    )
    private BigDecimal quantity;

    @Column(
            name = "quantity_unit",
            length = 30
    )
    private String quantityUnit;

    @Column(
            name = "net_weight_kg",
            precision = 20,
            scale = 3
    )
    private BigDecimal netWeightKg;

    @Column(
            name = "source_record_id",
            length = 150
    )
    private String sourceRecordId;

    @Column(name = "source_record_date")
    private LocalDate sourceRecordDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "revision_status",
            nullable = false,
            length = 30
    )
    private TradeRevisionStatus revisionStatus =
            TradeRevisionStatus.ORIGINAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "data_status",
            nullable = false,
            length = 30
    )
    private TradeDataStatus dataStatus =
            TradeDataStatus.AVAILABLE;

    @Column(
            name = "retrieved_at",
            nullable = false
    )
    private OffsetDateTime retrievedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (retrievedAt == null) {
            retrievedAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (revisionStatus == null) {
            revisionStatus = TradeRevisionStatus.ORIGINAL;
        }

        if (dataStatus == null) {
            dataStatus = TradeDataStatus.AVAILABLE;
        }
    }
}