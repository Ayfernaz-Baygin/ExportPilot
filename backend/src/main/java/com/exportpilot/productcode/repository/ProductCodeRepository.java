package com.exportpilot.productcode.repository;

import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.entity.ProductCodeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCodeRepository
        extends JpaRepository<ProductCode, Long> {

    List<ProductCode> findAllByActiveTrueOrderByCodeAsc();

    List<ProductCode>
    findAllByProductIdAndActiveTrueOrderByCodeAsc(
            Long productId
    );

    Optional<ProductCode> findByIdAndActiveTrue(Long id);

    Optional<ProductCode>
    findByCodeAndCodeTypeAndActiveTrue(
            String code,
            ProductCodeType codeType
    );

    boolean existsByCodeAndCodeType(
            String code,
            ProductCodeType codeType
    );
}