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
        // Given
        String name = "Electrónicos";
        String description = "Categoría de productos electrónicos";

        // La categoría que llega al caso de uso no debe tener ID (se valida internamente)
        Category categoryToCreate = Category.builder()
                .id(null)
                .name(name)
                .description(description)
                .build();

        // Simulamos lo que devolvería el repositorio después de guardar
        Category savedCategory = Category.builder()
                .id(UUID.randomUUID().toString()) // Simula el ID generado por la BD
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(Mono.just(savedCategory));

        // When
        Mono<Category> result = createCategoryUseCase.createCategory(categoryToCreate);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(category ->
                        category.getId() != null &&
                                category.getName().equals(name) &&
                                category.getDescription().equals(description) &&
                                category.getCreatedAt() != null &&
                                category.getUpdatedAt() != null
                )
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).save(any(Category.class));
    }


    @Test
    void testCreateCategory_withExistingId_shouldFail() {
        Category categoryWithId = Category.builder()
                .id(UUID.randomUUID().toString())
                .name("Electrónicos")
                .description("Categoría de productos electrónicos")
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