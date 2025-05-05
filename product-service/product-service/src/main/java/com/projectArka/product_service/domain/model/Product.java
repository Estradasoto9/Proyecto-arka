package com.projectArka.product_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
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
    private List<ProductFeature> features;

    public static Product create(String sku, String name, String description,
                                 BigDecimal price, UUID categoryId,
                                 UUID brandId, Integer stock, List<ProductFeature> features) {
        return Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .categoryId(categoryId)
                .brandId(brandId)
                .stock(stock)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(features)
                .build();
    }

}