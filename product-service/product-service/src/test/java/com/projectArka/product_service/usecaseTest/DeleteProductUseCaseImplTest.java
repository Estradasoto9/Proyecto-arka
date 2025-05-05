package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.DeleteProductUseCaseImpl;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

class DeleteProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private DeleteProductUseCaseImpl deleteProductUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteProductById_success() {
        UUID productId = UUID.randomUUID();
        when(productRepositoryPort.deleteById(productId.toString())).thenReturn(Mono.empty());

        Mono<Void> result = deleteProductUseCase.deleteProductById(productId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepositoryPort, times(1)).deleteById(productId.toString());
    }

    @Test
    void testDeleteProductById_repositoryThrowsException() {
        UUID productId = UUID.randomUUID();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(productRepositoryPort.deleteById(productId.toString())).thenReturn(Mono.error(repositoryException));

        Mono<Void> result = deleteProductUseCase.deleteProductById(productId);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productRepositoryPort, times(1)).deleteById(productId.toString());
    }
}