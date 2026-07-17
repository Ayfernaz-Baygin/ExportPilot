package com.exportpilot.economicindicator.controller;

import com.exportpilot.economicindicator.client.WorldBankClient;
import com.exportpilot.economicindicator.dto.WorldBankIndicatorResult;
import com.exportpilot.economicindicator.entity.EconomicIndicatorType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/world-bank")
public class WorldBankTestController {

    private final WorldBankClient worldBankClient;

    public WorldBankTestController(
            WorldBankClient worldBankClient
    ) {
        this.worldBankClient = worldBankClient;
    }

    @GetMapping("/indicator")
    public ResponseEntity<WorldBankIndicatorResult> getIndicator(
            @RequestParam String countryCode,
            @RequestParam EconomicIndicatorType indicatorType,
            @RequestParam Integer startYear,
            @RequestParam Integer endYear
    ) {
        WorldBankIndicatorResult result =
                worldBankClient.fetchIndicator(
                        countryCode,
                        indicatorType,
                        startYear,
                        endYear
                );

        return ResponseEntity.ok(result);
    }
}