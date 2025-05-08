package com.projectArka.product_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ProductResponseDTO {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private UUID categoryId;
    private UUID brandId;
    private Integer stock;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductFeatureDTO> features;

}