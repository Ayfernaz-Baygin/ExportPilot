package com.exportpilot.country.repository;

import com.exportpilot.country.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository
        extends JpaRepository<Country, Long> {

    List<Country> findAllByActiveTrueOrderByNameAsc();

    Optional<Country> findByIso2CodeIgnoreCase(String iso2Code);

    Optional<Country> findByIso3CodeIgnoreCase(String iso3Code);

    Optional<Country> findByUnM49Code(Integer unM49Code);

    List<Country> findAllByRegionIgnoreCaseAndActiveTrueOrderByNameAsc(
            String region
    );
}