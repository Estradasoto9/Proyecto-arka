package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.exception.CategoryAlreadyExistsException;
import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.CreateCategoryPort;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CreateCategoryUseCaseImpl implements CreateCategoryPort {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CreateCategoryUseCaseImpl(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public Mono<Category> createCategory(Category category) {
        if (category.getId() != null) {
            return Mono.error(new IllegalArgumentException("The new category must not have an ID"));
        }

        return categoryRepositoryPort.findByName(category.getName())
                .flatMap(existingCategory ->
                        Mono.<Category>error(new CategoryAlreadyExistsException("Category with the name already exists: " + category.getName()))
                )
                .switchIfEmpty(
                        Mono.defer(() -> categoryRepositoryPort.save(Category.builder()
                                .name(category.getName())
                                .description(category.getDescription())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()))
                );
    }
}