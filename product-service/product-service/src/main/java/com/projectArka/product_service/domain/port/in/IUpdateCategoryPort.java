package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Category;
import reactor.core.publisher.Mono;

public interface UpdateCategoryPort {
    Mono<Category> updateCategory(Category category);
}