package com.exportpilot.product.mapper;

import com.exportpilot.product.dto.ProductResponse;
import com.exportpilot.product.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);
}