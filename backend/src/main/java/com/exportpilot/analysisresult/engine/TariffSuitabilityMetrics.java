package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record TariffSuitabilityMetrics(

        BigDecimal tariffRateScore,

        BigDecimal preferentialAccessScore,

        BigDecimal nonTariffBarrierScore,

        BigDecimal tariffSuitabilityScore,

        BigDecimal appliedTariffRatePercent,

        Boolean preferentialTradeAgreement,

        BigDecimal nonTariffBarrierIndex,

        BigDecimal dataCompleteness

) {
}