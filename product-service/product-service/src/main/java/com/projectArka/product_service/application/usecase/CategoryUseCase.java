package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.exception.CategoryAlreadyExistsException;
import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.ICreateCategoryPort;
import com.projectArka.product_service.domain.port.in.IDeleteCategoryPort;
import com.projectArka.product_service.domain.port.in.IGetCategoryPort;
import com.projectArka.product_service.domain.port.in.IUpdateCategoryPort;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CategoryUseCaseIIII implements ICreateCategoryPort, IGetCategoryPort, IUpdateCategoryPort, IDeleteCategoryPort {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryUseCaseIIII(CategoryRepositoryPort categoryRepositoryPort) {
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

    @Override
    public Mono<Void> deleteCategoryById(UUID id) {
        return categoryRepositoryPort.deleteById(id);
    }

    @Override
    public Mono<Category> getCategoryById(UUID id) {
        return categoryRepositoryPort.findById(id.toString());
    }

    @Override
    public Flux<Category> getAllCategories() {
        return categoryRepositoryPort.findAll();
    }

    @Override
    public Mono<Category> getCategoryByName(String name) {
        return categoryRepositoryPort.findByName(name);
    }

    @Override
    public Mono<Category> updateCategory(Category category) {
        return categoryRepositoryPort.save(category);
    }
}