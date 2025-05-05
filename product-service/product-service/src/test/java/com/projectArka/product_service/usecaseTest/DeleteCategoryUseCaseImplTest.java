package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.DeleteCategoryUseCaseImpl;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

class DeleteCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private DeleteCategoryUseCaseImpl deleteCategoryUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteCategoryById_success() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepositoryPort.deleteById(categoryId)).thenReturn(Mono.empty());

        Mono<Void> result = deleteCategoryUseCase.deleteCategoryById(categoryId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).deleteById(categoryId);
    }

    @Test
    void testDeleteCategoryById_repositoryThrowsException() {
        UUID categoryId = UUID.randomUUID();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(categoryRepositoryPort.deleteById(categoryId)).thenReturn(Mono.error(repositoryException));

        Mono<Void> result = deleteCategoryUseCase.deleteCategoryById(categoryId);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(categoryRepositoryPort, times(1)).deleteById(categoryId);
    }
}