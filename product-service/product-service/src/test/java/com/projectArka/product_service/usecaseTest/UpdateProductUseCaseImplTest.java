package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.UpdateProductUseCaseImpl;
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

class UpdateProductUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private UpdateProductUseCaseImpl updateProductUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateProduct_success() {
        String productId = UUID.randomUUID().toString();
        Product productToUpdate = Product.builder()
                .id(productId)
                .sku("PROD-001")
                .name("Updated Product")
                .description("Updated description")
                .price(new BigDecimal("20.00"))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(150)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();

        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(productToUpdate));

        Mono<Product> result = updateProductUseCase.updateProduct(productToUpdate);

        StepVerifier.create(result)
                .expectNext(productToUpdate)
                .verifyComplete();

        verify(productRepositoryPort, times(1)).save(productToUpdate);
    }

    @Test
    void testUpdateProduct_repositoryThrowsException() {
        String productId = UUID.randomUUID().toString();
        Product productToUpdate = Product.builder()
                .id(productId)
                .sku("PROD-001")
                .name("Updated Product")
                .description("Updated description")
                .price(new BigDecimal("20.00"))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(150)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .features(Collections.emptyList())
                .build();
        RuntimeException repositoryException = new RuntimeException("Database error");
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.error(repositoryException));

        Mono<Product> result = updateProductUseCase.updateProduct(productToUpdate);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productRepositoryPort, times(1)).save(productToUpdate);
    }
}