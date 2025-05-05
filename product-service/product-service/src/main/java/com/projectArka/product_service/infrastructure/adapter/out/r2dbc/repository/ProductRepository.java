package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository;

import com.projectArka.product_service.infrastructure.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository extends R2dbcRepository<ProductEntity, UUID> {
    Mono<ProductEntity> findBySku(String sku);
}