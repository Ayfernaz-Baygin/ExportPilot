package com.exportpilot.trade.service;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.trade.dto.TradeRecordResponse;
import com.exportpilot.trade.entity.TradeFlow;
import com.exportpilot.trade.entity.TradeRecord;
import com.exportpilot.trade.mapper.TradeRecordMapper;
import com.exportpilot.trade.repository.TradeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeRecordService {

    private final TradeRecordRepository tradeRecordRepository;
    private final TradeRecordMapper tradeRecordMapper;

    public TradeRecordService(
            TradeRecordRepository tradeRecordRepository,
            TradeRecordMapper tradeRecordMapper
    ) {
        this.tradeRecordRepository = tradeRecordRepository;
        this.tradeRecordMapper = tradeRecordMapper;
    }

    @Transactional(readOnly = true)
    public List<TradeRecordResponse> getTradeRecords() {
        return tradeRecordMapper.toResponseList(
                tradeRecordRepository.findAllByOrderByTradeYearDescIdAsc()
        );
    }

    @Transactional(readOnly = true)
    public TradeRecordResponse getTradeRecordById(Long id) {
        TradeRecord tradeRecord = tradeRecordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trade record not found with id: " + id
                        )
                );

        return tradeRecordMapper.toResponse(tradeRecord);
    }

    @Transactional(readOnly = true)
    public List<TradeRecordResponse> searchTradeRecords(
            Long productCodeId,
            Long reporterCountryId,
            TradeFlow tradeFlow,
            Integer startYear,
            Integer endYear
    ) {
        validateSearchParameters(
                productCodeId,
                reporterCountryId,
                tradeFlow,
                startYear,
                endYear
        );

        List<TradeRecord> tradeRecords =
                tradeRecordRepository
                        .findAllByProductCodeIdAndReporterCountryIdAndTradeFlowAndTradeYearBetweenOrderByTradeYearAsc(
                                productCodeId,
                                reporterCountryId,
                                tradeFlow,
                                startYear,
                                endYear
                        );

        return tradeRecordMapper.toResponseList(tradeRecords);
    }

    private void validateSearchParameters(
            Long productCodeId,
            Long reporterCountryId,
            TradeFlow tradeFlow,
            Integer startYear,
            Integer endYear
    ) {
        if (productCodeId == null) {
            throw new BusinessRuleException(
                    "Product code ID is required."
            );
        }

        if (reporterCountryId == null) {
            throw new BusinessRuleException(
                    "Reporter country ID is required."
            );
        }

        if (tradeFlow == null) {
            throw new BusinessRuleException(
                    "Trade flow is required."
            );
        }

        if (startYear == null || endYear == null) {
            throw new BusinessRuleException(
                    "Start year and end year are required."
            );
        }

        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }
    }
}