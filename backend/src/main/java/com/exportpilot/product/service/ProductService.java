package com.exportpilot.product.service;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.product.dto.ProductResponse;
import com.exportpilot.product.entity.Product;
import com.exportpilot.product.mapper.ProductMapper;
import com.exportpilot.product.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 50;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        List<Product> products =
                productRepository
                        .findAllByActiveTrueOrderByNameAsc();

        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active product not found with id: "
                                        + id
                        )
                );

        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(
            String query,
            Integer limit
    ) {
        if (query == null || query.isBlank()) {
            throw new BusinessRuleException(
                    "Product search query is required."
            );
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < 2) {
            throw new BusinessRuleException(
                    "Product search query must contain "
                            + "at least 2 characters."
            );
        }

        int resolvedLimit = resolveSearchLimit(limit);

        Pageable pageable = PageRequest.of(
                0,
                resolvedLimit,
                Sort.by(
                        Sort.Direction.ASC,
                        "name"
                )
        );

        List<Product> products =
                productRepository
                        .findAllByActiveTrueAndNameContainingIgnoreCase(
                                normalizedQuery,
                                pageable
                        )
                        .getContent();

        return productMapper.toResponseList(products);
    }

    private int resolveSearchLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SEARCH_LIMIT;
        }

        if (limit < 1 || limit > MAX_SEARCH_LIMIT) {
            throw new BusinessRuleException(
                    "Product search limit must be between 1 and "
                            + MAX_SEARCH_LIMIT
                            + "."
            );
        }

        return limit;
    }
}