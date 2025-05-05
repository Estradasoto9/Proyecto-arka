package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.CreateProductUseCaseImpl;
import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private CreateProductUseCaseImpl createProductUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct_success() {
        // Given
        String sku = "PROD-001";
        String name = "Producto de Prueba";
        String description = "Descripción de prueba";
        BigDecimal price = new BigDecimal("10.00");
        UUID categoryId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        Integer stock = 100;

        // El producto que se pasa al caso de uso debe tener id = null
        Product productToCreate = Product.builder()
                .id(null)
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .categoryId(categoryId)
                .brandId(brandId)
                .stock(stock)
                .active(true)
                .features(Collections.emptyList())
                .build();

        // El producto simulado como "guardado" por el repositorio (con ID y timestamps)
        Product savedProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .categoryId(categoryId)
                .brandId(brandId)
                .stock(stock)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();

        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

        // When
        Mono<Product> result = createProductUseCase.createProduct(productToCreate);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getId() != null &&
                                product.getSku().equals(sku) &&
                                product.getName().equals(name) &&
                                product.getDescription().equals(description) &&
                                product.getPrice().compareTo(price) == 0 &&
                                product.getCategoryId().equals(categoryId) &&
                                product.getBrandId().equals(brandId) &&
                                product.getStock().equals(stock) &&
                                product.getCreatedAt() != null &&
                                product.getUpdatedAt() != null
                )
                .verifyComplete();

        verify(productRepositoryPort, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_withExistingId_shouldFail() {
        Product productWithId = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku("PROD-001")
                .name("Producto de Prueba")
                .description("Descripción de prueba")
                .price(new BigDecimal("10.00"))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(100)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();

        Mono<Product> result = createProductUseCase.createProduct(productWithId);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(productRepositoryPort, never()).save(any(Product.class));
    }
}
