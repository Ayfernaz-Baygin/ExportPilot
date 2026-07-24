package com.exportpilot.trade.provider;

import com.exportpilot.country.entity.Country;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.trade.entity.TradeRecord;

import java.util.List;

public interface TradeDataProvider {

    TradeDataSourceType getType();

    String getSourceName();

    List<TradeRecord> generateRecords(
            ProductCode productCode,
            Integer startYear,
            Integer endYear,
            Country germany,
            Country poland,
            Country romania,
            Country turkey
    );
}