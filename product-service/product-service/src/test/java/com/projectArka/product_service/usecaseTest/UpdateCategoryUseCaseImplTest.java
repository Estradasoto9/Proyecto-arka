package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.UpdateCategoryUseCaseImpl;
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

class UpdateCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private UpdateCategoryUseCaseImpl updateCategoryUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateCategory_success() {
        String categoryId = UUID.randomUUID().toString();
        Category categoryToUpdate = Category.builder()
                .id(categoryId)
                .name("Updated Category")
                .description("Updated description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(Mono.just(categoryToUpdate));

        Mono<Category> result = updateCategoryUseCase.updateCategory(categoryToUpdate);

        StepVerifier.create(result)
                .expectNext(categoryToUpdate)
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).save(categoryToUpdate);
    }

    @Test
    void testUpdateCategory_repositoryThrowsException() {
        String categoryId = UUID.randomUUID().toString();
        Category categoryToUpdate = Category.builder()
                .id(categoryId)
                .name("Updated Category")
                .description("Updated description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(categoryRepositoryPort.save(any(Category.class))).thenReturn(Mono.error(repositoryException));

        Mono<Category> result = updateCategoryUseCase.updateCategory(categoryToUpdate);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(categoryRepositoryPort, times(1)).save(categoryToUpdate);
    }
}