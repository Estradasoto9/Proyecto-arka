package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository;

import com.projectArka.product_service.infrastructure.entity.CategoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryRepository extends ReactiveCrudRepository<CategoryEntity, UUID> {
     Mono<CategoryEntity> findByName(String name);
}