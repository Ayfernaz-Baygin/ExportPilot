package com.exportpilot.productcode.repository;

import com.exportpilot.productcode.entity.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCodeRepository
        extends JpaRepository<ProductCode, Long> {

    List<ProductCode> findAllByActiveTrueOrderByCodeAsc();

    List<ProductCode> findAllByProductIdAndActiveTrueOrderByCodeAsc(
            Long productId
    );
}