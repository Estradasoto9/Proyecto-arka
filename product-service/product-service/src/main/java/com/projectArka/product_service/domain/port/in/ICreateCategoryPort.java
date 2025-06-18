package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Category;
import reactor.core.publisher.Mono;

public interface CreateCategoryPort {
    Mono<Category> createCategory(Category category);
}

