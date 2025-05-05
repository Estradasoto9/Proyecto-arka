package com.projectArka.product_service.application.usecase;

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
            return Mono.error(new IllegalArgumentException("New category must not have ID"));
        }

        return Mono.just(category)
                .map(p -> {
                    return Category.builder()
                            .id(null)
                            .name(p.getName())
                            .description(p.getDescription())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                })
                .flatMap(categoryRepositoryPort::save);
    }
}