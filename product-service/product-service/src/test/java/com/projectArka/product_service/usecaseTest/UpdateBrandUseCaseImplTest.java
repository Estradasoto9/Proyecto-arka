package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.UpdateBrandUseCaseImpl;
import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
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

class UpdateBrandUseCaseImplTest {

    @Mock
    private BrandRepositoryPort brandRepositoryPort;

    @InjectMocks
    private UpdateBrandUseCaseImpl updateBrandUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateBrand_success() {
        String brandId = UUID.randomUUID().toString();
        Brand brandToUpdate = Brand.builder()
                .id(brandId)
                .name("Marca Actualizada")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(brandRepositoryPort.save(any(Brand.class))).thenReturn(Mono.just(brandToUpdate));

        Mono<Brand> result = updateBrandUseCase.updateBrand(brandToUpdate);

        StepVerifier.create(result)
                .expectNext(brandToUpdate)
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).save(brandToUpdate);
    }

    @Test
    void testUpdateBrand_repositoryThrowsException() {
        String brandId = UUID.randomUUID().toString();
        Brand brandToUpdate = Brand.builder()
                .id(brandId)
                .name("Marca Actualizada")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(brandRepositoryPort.save(any(Brand.class))).thenReturn(Mono.error(repositoryException));

        Mono<Brand> result = updateBrandUseCase.updateBrand(brandToUpdate);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(brandRepositoryPort, times(1)).save(brandToUpdate);
    }
}