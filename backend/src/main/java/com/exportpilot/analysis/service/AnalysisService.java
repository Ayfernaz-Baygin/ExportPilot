package com.exportpilot.analysis.service;

import com.exportpilot.analysis.dto.AnalysisResponse;
import com.exportpilot.analysis.dto.CreateAnalysisRequest;
import com.exportpilot.analysis.entity.Analysis;
import com.exportpilot.analysis.entity.AnalysisStatus;
import com.exportpilot.analysis.mapper.AnalysisMapper;
import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.common.exception.ResourceNotFoundException;
import com.exportpilot.product.entity.Product;
import com.exportpilot.product.repository.ProductRepository;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.repository.ProductCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ProductRepository productRepository;
    private final ProductCodeRepository productCodeRepository;
    private final AnalysisMapper analysisMapper;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            ProductRepository productRepository,
            ProductCodeRepository productCodeRepository,
            AnalysisMapper analysisMapper
    ) {
        this.analysisRepository = analysisRepository;
        this.productRepository = productRepository;
        this.productCodeRepository = productCodeRepository;
        this.analysisMapper = analysisMapper;
    }

    @Transactional
    public AnalysisResponse createAnalysis(
            CreateAnalysisRequest request
    ) {
        validateYearRange(request.startYear(), request.endYear());

        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: "
                                        + request.productId()
                        )
                );

        ProductCode productCode = productCodeRepository
                .findById(request.productCodeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product code not found with id: "
                                        + request.productCodeId()
                        )
                );

        validateProductCodeBelongsToProduct(product, productCode);

        Analysis analysis = Analysis.builder()
                .product(product)
                .productCode(productCode)
                .startYear(request.startYear())
                .endYear(request.endYear())
                .targetRegion(normalizeTargetRegion(request.targetRegion()))
                .maxCountries(resolveMaxCountries(request.maxCountries()))
                .status(AnalysisStatus.PENDING)
                .scoringModelVersion("v1")
                .build();

        Analysis savedAnalysis = analysisRepository.save(analysis);

        return analysisMapper.toResponse(savedAnalysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> getAnalyses() {
        return analysisMapper.toResponseList(
                analysisRepository.findAllByOrderByCreatedAtDesc()
        );
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysisById(Long id) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis not found with id: " + id
                        )
                );

        return analysisMapper.toResponse(analysis);
    }

    private void validateYearRange(
            Integer startYear,
            Integer endYear
    ) {
        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }
    }

    private void validateProductCodeBelongsToProduct(
            Product product,
            ProductCode productCode
    ) {
        if (!productCode.getProduct().getId().equals(product.getId())) {
            throw new BusinessRuleException(
                    "The selected product code does not belong "
                            + "to the selected product."
            );
        }
    }

    private Integer resolveMaxCountries(Integer maxCountries) {
        return maxCountries == null ? 20 : maxCountries;
    }

    private String normalizeTargetRegion(String targetRegion) {
        if (targetRegion == null || targetRegion.isBlank()) {
            return null;
        }

        return targetRegion.trim();
    }
}