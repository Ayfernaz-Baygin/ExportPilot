package com.exportpilot.productcode.controller;

import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.service.ProductCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-codes")
public class ProductCodeController {

    private final ProductCodeService productCodeService;

    public ProductCodeController(
            ProductCodeService productCodeService
    ) {
        this.productCodeService = productCodeService;
    }

    @GetMapping
    public ResponseEntity<List<ProductCodeResponse>>
    getActiveProductCodes() {
        return ResponseEntity.ok(
                productCodeService.getActiveProductCodes()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCodeResponse> getProductCodeById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                productCodeService.getProductCodeById(id)
        );
    }
}