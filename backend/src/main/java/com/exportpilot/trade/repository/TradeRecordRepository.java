package com.exportpilot.trade.repository;

import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRecordRepository
        extends JpaRepository<TradeRecord, Long> {

    List<TradeRecord> findAllByOrderByTradeYearDescIdAsc();

    List<TradeRecord>
    findAllByProductCodeIdOrderByTradeYearDesc(
            Long productCodeId
    );

    List<TradeRecord>
    findAllByReporterCountryIdOrderByTradeYearDesc(
            Long reporterCountryId
    );

    List<TradeRecord>
    findAllByProductCodeIdAndReporterCountryIdAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
            Long productCodeId,
            Long reporterCountryId,
            TradeFlow tradeFlow,
            Integer startYear,
            Integer endYear
    );

    List<TradeRecord>
    findAllByProductCodeIdAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
            Long productCodeId,
            TradeFlow tradeFlow,
            Integer startYear,
            Integer endYear
    );
}