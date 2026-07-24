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
import com.exportpilot.productcode.validation.ProductCodeValidator;
import com.exportpilot.trade.provider.TradeDataSourceType;
import com.exportpilot.trade.service.TradeDataImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisService {

    private static final int DEFAULT_MAX_COUNTRIES = 20;
    private static final int MAX_ALLOWED_COUNTRIES = 100;

    private final AnalysisRepository analysisRepository;
    private final ProductRepository productRepository;
    private final ProductCodeRepository productCodeRepository;
    private final ProductCodeValidator productCodeValidator;
    private final AnalysisMapper analysisMapper;
    private final TradeDataImportService tradeDataImportService;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            ProductRepository productRepository,
            ProductCodeRepository productCodeRepository,
            ProductCodeValidator productCodeValidator,
            AnalysisMapper analysisMapper,
            TradeDataImportService tradeDataImportService
    ) {
        this.analysisRepository =
                analysisRepository;

        this.productRepository =
                productRepository;

        this.productCodeRepository =
                productCodeRepository;

        this.productCodeValidator =
                productCodeValidator;

        this.analysisMapper =
                analysisMapper;

        this.tradeDataImportService =
                tradeDataImportService;
    }

    @Transactional
    public AnalysisResponse createAnalysis(
            CreateAnalysisRequest request
    ) {
        if (request == null) {
            throw new BusinessRuleException(
                    "Analysis request is required."
            );
        }

        validateYearRange(
                request.startYear(),
                request.endYear()
        );

        Product product =
                productRepository
                        .findByIdAndActiveTrue(
                                request.productId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active product not found with id: "
                                                + request.productId()
                                )
                        );

        ProductCode productCode =
                productCodeRepository
                        .findByIdAndActiveTrue(
                                request.productCodeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active product code not found "
                                                + "with id: "
                                                + request.productCodeId()
                                )
                        );

        validateProductCodeBelongsToProduct(
                product,
                productCode
        );

        productCodeValidator.validate(
                productCode
        );

        Analysis analysis =
                Analysis.builder()
                        .product(product)
                        .productCode(productCode)
                        .startYear(
                                request.startYear()
                        )
                        .endYear(
                                request.endYear()
                        )
                        .targetRegion(
                                normalizeTargetRegion(
                                        request.targetRegion()
                                )
                        )
                        .maxCountries(
                                resolveMaxCountries(
                                        request.maxCountries()
                                )
                        )
                        .status(
                                AnalysisStatus.PENDING
                        )
                        .scoringModelVersion("v1")
                        .build();

        Analysis savedAnalysis =
                analysisRepository.saveAndFlush(
                        analysis
                );

        /*
         * Analiz oluşturulduktan sonra gerçek UN Comtrade
         * verileri otomatik olarak içe aktarılır.
         */
        tradeDataImportService.fetchForAnalysis(
                savedAnalysis.getId(),
                TradeDataSourceType.UN_COMTRADE
        );

        return analysisMapper.toResponse(
                savedAnalysis
        );
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> getAnalyses() {
        return analysisMapper.toResponseList(
                analysisRepository
                        .findAllByOrderByCreatedAtDesc()
        );
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysisById(
            Long id
    ) {
        Analysis analysis =
                analysisRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Analysis not found with id: "
                                                + id
                                )
                        );

        return analysisMapper.toResponse(
                analysis
        );
    }

    private void validateYearRange(
            Integer startYear,
            Integer endYear
    ) {
        if (startYear == null
                || endYear == null) {
            throw new BusinessRuleException(
                    "Start year and end year are required."
            );
        }

        if (startYear < 2000
                || startYear > 2100) {
            throw new BusinessRuleException(
                    "Start year must be between 2000 and 2100."
            );
        }

        if (endYear < 2000
                || endYear > 2100) {
            throw new BusinessRuleException(
                    "End year must be between 2000 and 2100."
            );
        }

        if (startYear > endYear) {
            throw new BusinessRuleException(
                    "Start year cannot be greater than end year."
            );
        }

        int yearCount =
                endYear - startYear + 1;

        if (yearCount > 5) {
            throw new BusinessRuleException(
                    "UN Comtrade analysis supports "
                            + "a maximum of 5 years."
            );
        }
    }

    private void validateProductCodeBelongsToProduct(
            Product product,
            ProductCode productCode
    ) {
        if (productCode.getProduct() == null
                || productCode.getProduct().getId() == null
                || !productCode.getProduct()
                        .getId()
                        .equals(product.getId())) {

            throw new BusinessRuleException(
                    "The selected product code does not belong "
                            + "to the selected product."
            );
        }
    }

    private Integer resolveMaxCountries(
            Integer maxCountries
    ) {
        if (maxCountries == null) {
            return DEFAULT_MAX_COUNTRIES;
        }

        if (maxCountries < 1
                || maxCountries > MAX_ALLOWED_COUNTRIES) {

            throw new BusinessRuleException(
                    "Maximum country count must be between 1 and "
                            + MAX_ALLOWED_COUNTRIES
                            + "."
            );
        }

        return maxCountries;
    }

    private String normalizeTargetRegion(
            String targetRegion
    ) {
        if (targetRegion == null
                || targetRegion.isBlank()) {
            return null;
        }

        return targetRegion.trim();
    }
}