package com.exportpilot.country.controller;

import com.exportpilot.country.dto.CountryResponse;
import com.exportpilot.country.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
@Tag(
        name = "Countries",
        description = "Country catalogue operations"
)
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @Operation(
            summary = "List active countries",
            description = "Returns all active countries ordered alphabetically."
    )
    @GetMapping
    public ResponseEntity<List<CountryResponse>> getCountries(
            @RequestParam(required = false) String region
    ) {
        if (region == null || region.isBlank()) {
            return ResponseEntity.ok(
                    countryService.getActiveCountries()
            );
        }

        return ResponseEntity.ok(
                countryService.getCountriesByRegion(region.trim())
        );
    }

    @Operation(
            summary = "Get country by ISO2 code",
            description = "Returns the country matching the ISO2 code."
    )
    @GetMapping("/by-iso2")
    public ResponseEntity<CountryResponse> getCountryByIso2Code(
            @RequestParam String code
    ) {
        return ResponseEntity.ok(
                countryService.getCountryByIso2Code(code)
        );
    }

    @Operation(
            summary = "Get country by ISO3 code",
            description = "Returns the country matching the ISO3 code."
    )
    @GetMapping("/by-iso3")
    public ResponseEntity<CountryResponse> getCountryByIso3Code(
            @RequestParam String code
    ) {
        return ResponseEntity.ok(
                countryService.getCountryByIso3Code(code)
        );
    }
}