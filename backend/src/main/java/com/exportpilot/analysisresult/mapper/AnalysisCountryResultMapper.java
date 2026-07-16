package com.exportpilot.analysisresult.mapper;

import com.exportpilot.analysisresult.dto.AnalysisCountryResultResponse;
import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalysisCountryResultMapper {

    @Mapping(
            target = "analysisId",
            source = "analysis.id"
    )
    @Mapping(
            target = "countryId",
            source = "country.id"
    )
    @Mapping(
            target = "countryIso2Code",
            source = "country.iso2Code"
    )
    @Mapping(
            target = "countryIso3Code",
            source = "country.iso3Code"
    )
    @Mapping(
            target = "countryName",
            source = "country.name"
    )
    @Mapping(
            target = "countryRegion",
            source = "country.region"
    )
    AnalysisCountryResultResponse toResponse(
            AnalysisCountryResult result
    );

    List<AnalysisCountryResultResponse> toResponseList(
            List<AnalysisCountryResult> results
    );
}