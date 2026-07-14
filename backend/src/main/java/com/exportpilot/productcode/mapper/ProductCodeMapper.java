package com.exportpilot.productcode.mapper;

import com.exportpilot.productcode.dto.ProductCodeResponse;
import com.exportpilot.productcode.entity.ProductCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductCodeMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    ProductCodeResponse toResponse(ProductCode productCode);

    List<ProductCodeResponse> toResponseList(List<ProductCode> productCodes);
}