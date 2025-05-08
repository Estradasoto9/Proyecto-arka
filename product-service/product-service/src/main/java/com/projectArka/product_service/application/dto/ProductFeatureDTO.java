package com.projectArka.product_service.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductFeatureDTO {
    @NotBlank(message = "Feature name is required")
    private String name;
    @NotBlank(message = "Feature value is required")
    private String value;
}