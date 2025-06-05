package com.projectArka.product_service.application.mapper;

import com.projectArka.product_service.application.dto.CreateCategoryRequestDTO;
import com.projectArka.product_service.application.dto.CategoryResponseDTO;
import com.projectArka.product_service.application.dto.UpdateCategoryRequestDTO;
import com.projectArka.product_service.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequestDTO createCategoryRequestDTO);

    CategoryResponseDTO toDTO(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(UpdateCategoryRequestDTO updateCategoryRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateCategoryRequestDTO updateCategoryRequestDTO, @MappingTarget Category category);
}