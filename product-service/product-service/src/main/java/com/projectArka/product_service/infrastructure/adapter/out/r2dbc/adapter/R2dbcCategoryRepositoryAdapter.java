package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.adapter;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository.CategoryRepository;
import com.projectArka.product_service.infrastructure.entity.BrandEntity;
import com.projectArka.product_service.infrastructure.entity.CategoryEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class R2dbcCategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryRepository categoryRepository;

    public R2dbcCategoryRepositoryAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Mono<Category> save(Category category) {
        return categoryRepository.save(CategoryEntity.fromDomain(category))
                .map(CategoryEntity::toDomain);
    }

    @Override
    public Mono<Category> findById(String id) {
        UUID uuid = UUID.fromString(id);
        return categoryRepository.findById(uuid)
                .map(CategoryEntity::toDomain);
    }

    @Override
    public Flux<Category> findAll() {
        return categoryRepository.findAll()
                .map(CategoryEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return categoryRepository.deleteById(id);
    }

    @Override
    public Mono<Category> findByName(String name) {
        return categoryRepository.findByName(name)
                .map(CategoryEntity::toDomain);
    }
}
