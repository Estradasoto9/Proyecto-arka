package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.GetProductUseCaseImpl;
import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GetProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private GetProductUseCaseImpl getProductUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProductById_success() {
        UUID productId = UUID.randomUUID();
        Product expectedProduct = Product.builder()
                .id(productId.toString())
                .sku("PROD-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("10.00"))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();
        when(productRepositoryPort.findById(productId.toString())).thenReturn(Mono.just(expectedProduct));

        Mono<Product> result = getProductUseCase.getProductById(productId);

        StepVerifier.create(result)
                .expectNext(expectedProduct)
                .verifyComplete();

        verify(productRepositoryPort, times(1)).findById(productId.toString());
    }

    @Test
    void testGetProductById_notFound() {
        UUID productId = UUID.randomUUID();
        when(productRepositoryPort.findById(productId.toString())).thenReturn(Mono.empty());

        Mono<Product> result = getProductUseCase.getProductById(productId);

        StepVerifier.create(result)
                .verifyComplete();
        verify(productRepositoryPort, times(1)).findById(productId.toString());
    }

    @Test
    void testGetProductBySku_success() {
        String productSku = "PROD-001";
        Product expectedProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(productSku)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("10.00"))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();
        when(productRepositoryPort.findBySku(productSku)).thenReturn(Mono.just(expectedProduct));

        Mono<Product> result = getProductUseCase.getProductBySku(productSku);

        StepVerifier.create(result)
                .expectNext(expectedProduct)
                .verifyComplete();

        verify(productRepositoryPort, times(1)).findBySku(productSku);
    }

    @Test
    void testGetProductBySku_notFound() {
        String productSku = "NonExistentSku";
        when(productRepositoryPort.findBySku(productSku)).thenReturn(Mono.empty());

        Mono<Product> result = getProductUseCase.getProductBySku(productSku);

        StepVerifier.create(result)
                .verifyComplete();
        verify(productRepositoryPort, times(1)).findBySku(productSku);
    }

    @Test
    void testGetAllProducts_success() {
        Product product1 = Product.builder().id("1").sku("SKU-1").name("Product 1").description("Desc 1").price(BigDecimal.TEN).categoryId(UUID.randomUUID()).brandId(UUID.randomUUID()).stock(10).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).features(Collections.emptyList()).build();
        Product product2 = Product.builder().id("2").sku("SKU-2").name("Product 2").description("Desc 2").price(BigDecimal.TEN).categoryId(UUID.randomUUID()).brandId(UUID.randomUUID()).stock(10).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).features(Collections.emptyList()).build();
        when(productRepositoryPort.findAll()).thenReturn(Flux.just(product1, product2));

        Flux<Product> result = getProductUseCase.getAllProducts();

        StepVerifier.create(result)
                .expectNext(product1, product2)
                .verifyComplete();

        verify(productRepositoryPort, times(1)).findAll();
    }

    @Test
    void testGetAllProducts_empty() {
        when(productRepositoryPort.findAll()).thenReturn(Flux.empty());

        Flux<Product> result = getProductUseCase.getAllProducts();

        StepVerifier.create(result)
                .verifyComplete();
        verify(productRepositoryPort, times(1)).findAll();
    }
}