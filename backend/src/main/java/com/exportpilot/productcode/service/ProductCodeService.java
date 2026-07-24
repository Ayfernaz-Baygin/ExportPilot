package com.exportpilot.productcode.service;

import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.product.entity.Product;
import com.exportpilot.product.repository.ProductRepository;
import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.entity.ProductCodeType;
import com.exportpilot.productcode.mapper.ProductCodeMapper;
import com.exportpilot.productcode.repository.ProductCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class ProductCodeService {

    private final ProductCodeRepository productCodeRepository;
    private final ProductRepository productRepository;
    private final ProductCodeMapper productCodeMapper;

    public ProductCodeService(
            ProductCodeRepository productCodeRepository,
            ProductRepository productRepository,
            ProductCodeMapper productCodeMapper
    ) {
        this.productCodeRepository = productCodeRepository;
        this.productRepository = productRepository;
        this.productCodeMapper = productCodeMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductCodeResponse> getActiveProductCodes() {
        List<ProductCode> productCodes =
                productCodeRepository
                        .findAllByActiveTrueOrderByCodeAsc();

        return productCodeMapper.toResponseList(productCodes);
    }

    @Transactional(readOnly = true)
    public ProductCodeResponse getProductCodeById(Long id) {
        ProductCode productCode =
                productCodeRepository
                        .findByIdAndActiveTrue(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active product code not found "
                                                + "with id: "
                                                + id
                                )
                        );

        return productCodeMapper.toResponse(productCode);
    }

    @Transactional(readOnly = true)
    public List<ProductCodeResponse> getCodesByProductId(
            Long productId,
            ProductCodeType codeType,
            Short classificationLevel
    ) {
        Product product = productRepository
                .findByIdAndActiveTrue(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active product not found with id: "
                                        + productId
                        )
                );

        Stream<ProductCode> codeStream =
                productCodeRepository
                        .findAllByProductIdAndActiveTrueOrderByCodeAsc(
                                product.getId()
                        )
                        .stream();

        if (codeType != null) {
            codeStream = codeStream.filter(
                    productCode ->
                            productCode.getCodeType() == codeType
            );
        }

        if (classificationLevel != null) {
            codeStream = codeStream.filter(
                    productCode ->
                            classificationLevel.equals(
                                    productCode
                                            .getClassificationLevel()
                            )
            );
        }

        return codeStream
                .map(productCodeMapper::toResponse)
                .toList();
    }
}