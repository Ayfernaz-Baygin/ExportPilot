package com.exportpilot.productcode.validation;

import com.exportpilot.common.exception.BusinessRuleException;
import com.exportpilot.productcode.entity.ProductCode;
import com.exportpilot.productcode.entity.ProductCodeType;
import org.springframework.stereotype.Component;

@Component
public class ProductCodeValidator {

    public void validate(ProductCode productCode) {
        if (productCode == null) {
            throw new BusinessRuleException(
                    "Product code is required."
            );
        }

        String code = productCode.getCode();
        ProductCodeType codeType = productCode.getCodeType();
        Short classificationLevel =
                productCode.getClassificationLevel();

        if (code == null || code.isBlank()) {
            throw new BusinessRuleException(
                    "Product code cannot be empty."
            );
        }

        String normalizedCode = code.trim();

        if (!normalizedCode.matches("\\d+")) {
            throw new BusinessRuleException(
                    "Product code must contain only digits."
            );
        }

        if (codeType == null) {
            throw new BusinessRuleException(
                    "Product code type is required."
            );
        }

        if (classificationLevel == null) {
            throw new BusinessRuleException(
                    "Product code classification level is required."
            );
        }

        if (normalizedCode.length() != classificationLevel.intValue()) {
            throw new BusinessRuleException(
                    "Product code length does not match "
                            + "its classification level."
            );
        }

        if (!codeType.supportsLength(normalizedCode.length())) {
            if (codeType == ProductCodeType.HS) {
                throw new BusinessRuleException(
                        "HS code length must be 2, 4, or 6 digits."
                );
            }

            throw new BusinessRuleException(
                    "GTIP code length must be 12 digits."
            );
        }
    }
}