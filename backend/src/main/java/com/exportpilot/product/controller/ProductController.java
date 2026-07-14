package com.exportpilot.product.controller;

import com.exportpilot.product.dto.ProductResponse;
import com.exportpilot.product.service.ProductService;
import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.service.ProductCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(
        name = "Products",
        description = "Product catalogue operations"
)
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

    @Operation(
            summary = "List active products",
            description = "Returns all active products ordered alphabetically."
    )
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        return ResponseEntity.ok(
                productService.getActiveProducts()
        );
    }

    @Operation(
            summary = "Get product by ID",
            description = "Returns the product matching the specified identifier."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    @Operation(
            summary = "List product codes",
            description = "Returns active HS and GTIP codes linked to the product."
    )
    @GetMapping("/{id}/codes")
    public ResponseEntity<List<ProductCodeResponse>> getProductCodes(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                productCodeService.getCodesByProductId(id)
        );
    }
}