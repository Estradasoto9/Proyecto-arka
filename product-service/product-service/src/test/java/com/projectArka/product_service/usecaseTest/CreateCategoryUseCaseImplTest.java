package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.CreateCategoryUseCaseImpl;
import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private CreateCategoryUseCaseImpl createCategoryUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCategory_success() {
        String name = "Electronics";
        String description = "Category of electronic products";

        Category categoryToCreate = Category.builder()
                .id(null)
                .name(name)
                .description(description)
                .build();

        Category savedCategory = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.findByName(name)).thenReturn(Mono.empty());
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(Mono.just(savedCategory));

        Mono<Category> result = createCategoryUseCase.createCategory(categoryToCreate);

        StepVerifier.create(result)
                .expectNextMatches(category ->
                        category.getId() != null &&
                                category.getName().equals(name) &&
                                category.getDescription().equals(description) &&
                                category.getCreatedAt() != null &&
                                category.getUpdatedAt() != null
                )
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).findByName(name);
        verify(categoryRepositoryPort, times(1)).save(any(Category.class));
    }


    @Test
    void testCreateCategory_withExistingId_shouldFail() {
        Category categoryWithId = Category.builder()
                .id(UUID.randomUUID().toString())
                .name("Electronics")
                .description("Category of electronic products")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Mono<Category> result = createCategoryUseCase.createCategory(categoryWithId);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(categoryRepositoryPort, never()).save(any(Category.class));
    }
}