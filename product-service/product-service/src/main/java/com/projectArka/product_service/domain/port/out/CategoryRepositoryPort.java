package com.projectArka.product_service.domain.port.out;

import com.projectArka.product_service.domain.model.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryRepositoryPort {
    Mono<Category> save(Category category);
    Mono<Category> findById(String id);
    Mono<Category> findByName(String name);
    Flux<Category> findAll();
    Mono<Void> deleteById(UUID id);
}
