package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.GetBrandUseCaseImpl;
import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GetBrandUseCaseImplTest {

    @Mock
    private BrandRepositoryPort brandRepositoryPort;

    @InjectMocks
    private GetBrandUseCaseImpl getBrandUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBrandById_success() {
        UUID brandId = UUID.randomUUID();
        Brand expectedBrand = Brand.builder()
                .id(brandId.toString())
                .name("Marca de Prueba")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(brandRepositoryPort.findById(brandId.toString())).thenReturn(Mono.just(expectedBrand));

        Mono<Brand> result = getBrandUseCase.getBrandById(brandId);

        StepVerifier.create(result)
                .expectNext(expectedBrand)
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).findById(brandId.toString());
    }

    @Test
    void testGetBrandById_notFound() {
        UUID brandId = UUID.randomUUID();
        when(brandRepositoryPort.findById(brandId.toString())).thenReturn(Mono.empty());

        Mono<Brand> result = getBrandUseCase.getBrandById(brandId);

        StepVerifier.create(result)
                .verifyComplete(); //  Verifica que el Mono se complete sin emitir ningún elemento.
        verify(brandRepositoryPort, times(1)).findById(brandId.toString());
    }

    @Test
    void testGetBrandByName_success() {
        String brandName = "Marca de Prueba";
        Brand expectedBrand = Brand.builder()
                .id(UUID.randomUUID().toString())
                .name(brandName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(brandRepositoryPort.findByName(brandName)).thenReturn(Mono.just(expectedBrand));

        Mono<Brand> result = getBrandUseCase.getBrandByName(brandName);

        StepVerifier.create(result)
                .expectNext(expectedBrand)
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).findByName(brandName);
    }

    @Test
    void testGetBrandByName_notFound() {
        String brandName = "NonExistentBrand";
        when(brandRepositoryPort.findByName(brandName)).thenReturn(Mono.empty());

        Mono<Brand> result = getBrandUseCase.getBrandByName(brandName);

        StepVerifier.create(result)
                .verifyComplete();
        verify(brandRepositoryPort, times(1)).findByName(brandName);
    }

    @Test
    void testGetAllBrands_success() {
        Brand brand1 = Brand.builder().id("1").name("Marca 1").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        Brand brand2 = Brand.builder().id("2").name("Marca 2").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(brandRepositoryPort.findAll()).thenReturn(Flux.just(brand1, brand2));

        Flux<Brand> result = getBrandUseCase.getAllBrands();


        StepVerifier.create(result)
                .expectNext(brand1, brand2)
                .verifyComplete();

        verify(brandRepositoryPort, times(1)).findAll();
    }

    @Test
    void testGetAllBrands_empty() {
        when(brandRepositoryPort.findAll()).thenReturn(Flux.empty());

        Flux<Brand> result = getBrandUseCase.getAllBrands();

        StepVerifier.create(result)
                .verifyComplete();
        verify(brandRepositoryPort, times(1)).findAll();
    }
}