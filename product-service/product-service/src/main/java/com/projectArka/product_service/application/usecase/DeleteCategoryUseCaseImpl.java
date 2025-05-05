package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.port.in.DeleteCategoryPort;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DeleteCategoryUseCaseImpl implements DeleteCategoryPort {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public DeleteCategoryUseCaseImpl(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public Mono<Void> deleteCategoryById(UUID id) {
        return categoryRepositoryPort.deleteById(id);
    }
}