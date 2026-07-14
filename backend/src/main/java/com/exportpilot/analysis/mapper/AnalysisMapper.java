package com.exportpilot.analysis.mapper;

import com.exportpilot.analysis.dto.AnalysisResponse;
import com.exportpilot.analysis.entity.Analysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productCodeId", source = "productCode.id")
    @Mapping(target = "productCode", source = "productCode.code")
    @Mapping(target = "productCodeType", source = "productCode.codeType")
    AnalysisResponse toResponse(Analysis analysis);

    List<AnalysisResponse> toResponseList(List<Analysis> analyses);
}