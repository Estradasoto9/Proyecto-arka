package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetCategoryPort {
    Mono<Category> getCategoryById(UUID id);
    Flux<Category> getAllCategories();
    Mono<Category> getCategoryByName(String name);
}
