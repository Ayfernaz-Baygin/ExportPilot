package com.exportpilot.product.controller;

import com.exportpilot.product.dto.ProductResponse;
import com.exportpilot.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.service.ProductCodeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCodeService productCodeService;

    public ProductController(
        ProductService productService,
        ProductCodeService productCodeService
) {
    this.productService = productService;
    this.productCodeService = productCodeService;
}

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/codes")
public ResponseEntity<List<ProductCodeResponse>> getProductCodes(
        @PathVariable Long id
) {
    return ResponseEntity.ok(
            productCodeService.getCodesByProductId(id)
    );
}
}