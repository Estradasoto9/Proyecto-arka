package com.projectArka.product_service.infrastructure.entity;

import com.projectArka.product_service.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public static CategoryEntity fromDomain(Category category) {
        LocalDateTime now = LocalDateTime.now();
        UUID id = null;
        if (category.getId() != null) {
            id = UUID.fromString(category.getId());
        }

        return CategoryEntity.builder()
                .id(id)
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt() != null ? category.getCreatedAt() : now)
                .updatedAt(now)
                .build();
    }

    public Category toDomain() {
        this.updatedAt = LocalDateTime.now();
        return Category.builder()
                .id(this.id != null ? this.id.toString() : null)
                .name(this.name)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}