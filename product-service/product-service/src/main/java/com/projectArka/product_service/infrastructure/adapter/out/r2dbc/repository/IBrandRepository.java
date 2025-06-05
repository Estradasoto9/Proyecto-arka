package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository;

import com.projectArka.product_service.infrastructure.entity.BrandEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BrandRepository extends R2dbcRepository<BrandEntity, UUID> {
    Mono<BrandEntity> findByName(String name);
    Mono<BrandEntity> save(BrandEntity entity);
    Mono<BrandEntity> findById(UUID id);
    reactor.core.publisher.Flux<BrandEntity> findAll();
    Mono<Void> deleteById(UUID id);
}