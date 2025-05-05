package com.projectArka.product_service.infrastructure.entity;

import com.projectArka.product_service.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {
    @Id
    @Column("id")
    private UUID id;

    @Column("sku")
    private String sku;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("price")
    private BigDecimal price;

    @Column("category_id")
    private UUID categoryId;

    @Column("brand_id")
    private UUID brandId;

    @Column("stock")
    private Integer stock;

    @Column("active")
    private Boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public static ProductEntity fromDomain(Product product) {
        if (product == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        UUID id = null;
        if (product.getId() != null) {
            id = UUID.fromString(product.getId());
        }

        return ProductEntity.builder()
                .id(id)
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .stock(product.getStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt() : now)
                .updatedAt(now)
                .build();
    }

    public Product toDomain() {
        this.updatedAt = LocalDateTime.now();
        return Product.builder()
                .id(this.id != null ? this.id.toString() : null)
                .sku(this.sku)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .categoryId(this.categoryId)
                .brandId(this.brandId)
                .stock(this.stock)
                .active(this.active)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}