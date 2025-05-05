package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.CreateBrandUseCaseImpl;
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

class CreateBrandUseCaseImplTest {

    @Mock
    private BrandRepositoryPort brandRepositoryPort;

    @InjectMocks
    private CreateBrandUseCaseImpl createBrandUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBrand_success() {
        // Given
        String name = "Samsung";
        Brand brandToCreate = Brand.builder()
                .id(null)
                .name(name)
                .build();

        Brand savedBrand = Brand.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(brandRepositoryPort.save(any(Brand.class))).thenReturn(Mono.just(savedBrand));

        Mono<Brand> result = createBrandUseCase.createBrand(brandToCreate);

        StepVerifier.create(result)
                .expectNextMatches(brand ->
                        brand.getId() != null &&
                                brand.getName().equals(name) &&
                                brand.getCreatedAt() != null &&
                                brand.getUpdatedAt() != null
                )
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).save(any(Brand.class));
    }

    @Test
    void testCreateBrand_withExistingId_shouldFail() {
        Brand brandWithId = Brand.builder()
                .id(UUID.randomUUID().toString())
                .name("Marca de Prueba")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Mono<Brand> result = createBrandUseCase.createBrand(brandWithId);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(brandRepositoryPort, never()).save(any(Brand.class));
    }
}