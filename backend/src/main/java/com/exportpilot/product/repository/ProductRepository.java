package com.exportpilot.product.repository;

import com.exportpilot.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findAllByActiveTrueOrderByNameAsc();

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findAllByActiveTrueAndNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);
}