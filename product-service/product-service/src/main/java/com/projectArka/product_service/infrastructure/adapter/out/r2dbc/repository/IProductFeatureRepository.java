package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository;

import com.projectArka.product_service.infrastructure.entity.ProductFeatureEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductFeatureRepository extends R2dbcRepository<ProductFeatureEntity, UUID> {

    Flux<ProductFeatureEntity> findByProductId(UUID productId);
    Mono<Void> deleteByProductId(UUID productId);
}