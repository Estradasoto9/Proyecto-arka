package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.DeleteBrandUseCaseImpl;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

class DeleteBrandUseCaseImplTest {

    @Mock
    private BrandRepositoryPort brandRepositoryPort;

    @InjectMocks
    private DeleteBrandUseCaseImpl deleteBrandUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteBrandById_success() {
        UUID brandId = UUID.randomUUID();
        when(brandRepositoryPort.deleteById(brandId.toString())).thenReturn(Mono.empty());

        Mono<Void> result = deleteBrandUseCase.deleteBrandById(brandId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).deleteById(brandId.toString());
    }

    @Test
    void testDeleteBrandById_repositoryThrowsException() {
        UUID brandId = UUID.randomUUID();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(brandRepositoryPort.deleteById(brandId.toString())).thenReturn(Mono.error(repositoryException));

        Mono<Void> result = deleteBrandUseCase.deleteBrandById(brandId);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(brandRepositoryPort, times(1)).deleteById(brandId.toString());
    }
}