package com.projectArka.product_service.infrastructure.entity;

import com.projectArka.product_service.domain.model.ProductFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;
import java.time.LocalDateTime;

@Table(name = "product_feature")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFeatureEntity {
    @Id
    @Column("id")
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("name")
    private String name;

    @Column("value")
    private String value;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public static ProductFeatureEntity fromDomain(ProductFeature productFeature) {
        if (productFeature == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return ProductFeatureEntity.builder()
                .name(productFeature.getName())
                .value(productFeature.getValue())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public ProductFeature toDomain() {
        return ProductFeature.builder()
                .name(this.name)
                .value(this.value)
                .build();
    }
}