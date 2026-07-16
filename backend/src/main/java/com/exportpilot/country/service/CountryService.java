package com.exportpilot.country.service;

import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.country.dto.CountryResponse;
import com.exportpilot.country.entity.Country;
import com.exportpilot.country.mapper.CountryMapper;
import com.exportpilot.country.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public CountryService(
            CountryRepository countryRepository,
            CountryMapper countryMapper
    ) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getActiveCountries() {
        return countryMapper.toResponseList(
                countryRepository.findAllByActiveTrueOrderByNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public CountryResponse getCountryByIso2Code(String iso2Code) {
        Country country = countryRepository
                .findByIso2CodeIgnoreCase(iso2Code)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with ISO2 code: "
                                        + iso2Code
                        )
                );

        return countryMapper.toResponse(country);
    }

    @Transactional(readOnly = true)
    public CountryResponse getCountryByIso3Code(String iso3Code) {
        Country country = countryRepository
                .findByIso3CodeIgnoreCase(iso3Code)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Country not found with ISO3 code: "
                                        + iso3Code
                        )
                );

        return countryMapper.toResponse(country);
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getCountriesByRegion(String region) {
        return countryMapper.toResponseList(
                countryRepository
                        .findAllByRegionIgnoreCaseAndActiveTrueOrderByNameAsc(
                                region
                        )
        );
    }
}