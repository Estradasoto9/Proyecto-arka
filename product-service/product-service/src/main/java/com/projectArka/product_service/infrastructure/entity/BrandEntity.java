package com.projectArka.product_service.infrastructure.entity;

import com.projectArka.product_service.domain.model.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("brand")
public class BrandEntity {
    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public static BrandEntity fromDomain(Brand brand) {
        if (brand == null) {
            return null;
        }
        UUID id = null;
        if (brand.getId() != null) {
            id = UUID.fromString(brand.getId());
        }
        LocalDateTime now = LocalDateTime.now();

        return BrandEntity.builder()
                .id(id)
                .name(brand.getName())
                .createdAt(brand.getCreatedAt() != null ? brand.getCreatedAt() : now)
                .updatedAt(now)
                .build();
    }

    public Brand toDomain() {
        return Brand.builder()
                .id(this.id != null ? this.id.toString() : null)
                .name(this.name)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
