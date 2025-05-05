package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.UpdateCategoryPort;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateCategoryUseCaseImpl implements UpdateCategoryPort {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public UpdateCategoryUseCaseImpl(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public Mono<Category> updateCategory(Category category) {
        return categoryRepositoryPort.save(category);
    }
}