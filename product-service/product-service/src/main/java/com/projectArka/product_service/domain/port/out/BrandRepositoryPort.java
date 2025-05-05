package com.projectArka.product_service.domain.port.out;

import com.projectArka.product_service.domain.model.Brand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BrandRepositoryPort {
    Mono<Brand> save(Brand brand);
    Mono<Brand> findById(String id);
    Mono<Brand> findByName(String name);
    Flux<Brand> findAll();
    Mono<Void> deleteById(String id);
}