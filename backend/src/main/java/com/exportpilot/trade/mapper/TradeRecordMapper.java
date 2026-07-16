package com.exportpilot.trade.mapper;

import com.exportpilot.trade.dto.TradeRecordResponse;
import com.exportpilot.trade.entity.TradeRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TradeRecordMapper {

    @Mapping(
            target = "reporterCountryId",
            source = "reporterCountry.id"
    )
    @Mapping(
            target = "reporterIso2Code",
            source = "reporterCountry.iso2Code"
    )
    @Mapping(
            target = "reporterCountryName",
            source = "reporterCountry.name"
    )
    @Mapping(
            target = "partnerCountryId",
            source = "partnerCountry.id"
    )
    @Mapping(
            target = "partnerIso2Code",
            source = "partnerCountry.iso2Code"
    )
    @Mapping(
            target = "partnerCountryName",
            source = "partnerCountry.name"
    )
    @Mapping(
            target = "productCodeId",
            source = "productCode.id"
    )
    @Mapping(
            target = "productCode",
            source = "productCode.code"
    )
    @Mapping(
            target = "productCodeType",
            source = "productCode.codeType"
    )
    @Mapping(
            target = "productDescription",
            source = "productCode.description"
    )
    TradeRecordResponse toResponse(TradeRecord tradeRecord);

    List<TradeRecordResponse> toResponseList(
            List<TradeRecord> tradeRecords
    );
}