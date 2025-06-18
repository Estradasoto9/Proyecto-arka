package com.projectArka.product_service.application.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.net.ProtocolFamily;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UpdateProductRequestDTO {
    private String sku;
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;
    private String description;
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    private UUID categoryId;
    private UUID brandId;
    private Integer stock;
    private Boolean active;
    private List<ProductFeatureDTO> features;

}