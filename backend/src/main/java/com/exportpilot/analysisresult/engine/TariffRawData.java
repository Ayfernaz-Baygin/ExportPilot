package com.exportpilot.analysisresult.engine;

import java.math.BigDecimal;

public record TariffRawData(

        BigDecimal appliedTariffRatePercent,

        Boolean preferentialTradeAgreement,

        BigDecimal nonTariffBarrierIndex,

        String tradeAgreementName,

        String dataSource,

        boolean estimated

) {
}