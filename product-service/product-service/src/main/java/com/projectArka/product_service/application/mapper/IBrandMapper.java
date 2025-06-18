package com.projectArka.product_service.application.mapper;

import com.projectArka.product_service.application.dto.CreateBrandRequestDTO;
import com.projectArka.product_service.application.dto.BrandResponseDTO;
import com.projectArka.product_service.application.dto.UpdateBrandRequestDTO;
import com.projectArka.product_service.domain.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Brand toEntity(CreateBrandRequestDTO createBrandRequestDTO);

    BrandResponseDTO toDTO(Brand brand);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Brand toEntity(UpdateBrandRequestDTO updateBrandRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateBrandRequestDTO updateBrandRequestDTO, @MappingTarget Brand brand);
}