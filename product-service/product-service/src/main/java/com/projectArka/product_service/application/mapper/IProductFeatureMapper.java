package com.projectArka.product_service.application.mapper;

import com.projectArka.product_service.application.dto.ProductFeatureDTO;
import com.projectArka.product_service.domain.model.ProductFeature;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductFeatureMapper {

    ProductFeature toEntity(ProductFeatureDTO dto);

    ProductFeatureDTO toDTO(ProductFeature entity);
}