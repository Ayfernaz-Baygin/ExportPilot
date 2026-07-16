package com.exportpilot.country.mapper;

import com.exportpilot.country.dto.CountryResponse;
import com.exportpilot.country.entity.Country;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);

    List<CountryResponse> toResponseList(List<Country> countries);
}