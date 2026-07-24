package com.exportpilot.trade.dto.uncomtrade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UnComtradeDataRow(
        String typeCode,
        String freqCode,

        Integer refPeriodId,
        Integer refYear,
        Integer refMonth,
        String period,

        Integer reporterCode,
        String reporterISO,
        String reporterDesc,

        String flowCode,
        String flowDesc,

        Integer partnerCode,
        String partnerISO,
        String partnerDesc,

        Integer partner2Code,
        String partner2ISO,
        String partner2Desc,

        String classificationCode,
        String classificationSearchCode,

        Boolean isOriginalClassification,

        String cmdCode,
        String cmdDesc,
        String aggrLevel,

        Boolean isLeaf,

        String customsCode,
        String customsDesc,

        String mosCode,
        String motCode,
        String motDesc,

        Integer qtyUnitCode,
        String qtyUnitAbbr,
        BigDecimal qty,
        Boolean isQtyEstimated,

        Integer altQtyUnitCode,
        String altQtyUnitAbbr,
        BigDecimal altQty,
        Boolean isAltQtyEstimated,

        BigDecimal netWgt,
        Boolean isNetWgtEstimated,

        BigDecimal grossWgt,
        Boolean isGrossWgtEstimated,

        BigDecimal cifvalue,
        BigDecimal fobvalue,
        BigDecimal primaryValue,

        Boolean isReported,
        Boolean isAggregate
) {
}