package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.GetCategoryPort;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetCategoryUseCaseImpl implements GetCategoryPort {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public GetCategoryUseCaseImpl(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
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
}
