package com.exportpilot.productcode.service;

import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.mapper.ProductCodeMapper;
import com.exportpilot.productcode.repository.ProductCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exportpilot.common.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class ProductCodeService {

    private final ProductCodeRepository productCodeRepository;
    private final ProductCodeMapper productCodeMapper;

    public ProductCodeService(
            ProductCodeRepository productCodeRepository,
            ProductCodeMapper productCodeMapper
    ) {
        this.productCodeRepository = productCodeRepository;
        this.productCodeMapper = productCodeMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductCodeResponse> getActiveProductCodes() {
        List<ProductCode> productCodes =
                productCodeRepository.findAllByActiveTrueOrderByCodeAsc();

        return productCodeMapper.toResponseList(productCodes);
    }

    @Transactional(readOnly = true)
    public ProductCodeResponse getProductCodeById(Long id) {
        ProductCode productCode = productCodeRepository.findById(id)
                .orElseThrow(() ->
        new ResourceNotFoundException(
                "Product code not found with id: " + id
        )
   );
            

        return productCodeMapper.toResponse(productCode);
    }

    @Transactional(readOnly = true)
    public List<ProductCodeResponse> getCodesByProductId(Long productId) {
        List<ProductCode> productCodes =
                productCodeRepository
                        .findAllByProductIdAndActiveTrueOrderByCodeAsc(
                                productId
                        );

        return productCodeMapper.toResponseList(productCodes);
    }
}