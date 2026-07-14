package com.exportpilot.product.service;

import com.exportpilot.product.dto.ProductResponse;
import com.exportpilot.product.entity.Product;
import com.exportpilot.product.mapper.ProductMapper;
import com.exportpilot.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exportpilot.common.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ProductService {

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
                productRepository.findAllByActiveTrueOrderByNameAsc();

        return productMapper.toResponseList(products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
        new ResourceNotFoundException(
                "Product not found with id: " + id
        )
);

        return productMapper.toResponse(product);
    }
}