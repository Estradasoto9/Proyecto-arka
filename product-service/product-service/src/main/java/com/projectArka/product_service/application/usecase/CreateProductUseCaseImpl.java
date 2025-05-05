package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.CreateProductPort;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CreateProductUseCaseImpl implements CreateProductPort {

    private final ProductRepositoryPort productRepositoryPort;

    public CreateProductUseCaseImpl(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        if (product.getId() != null) {
            return Mono.error(new IllegalArgumentException("New product must not have ID"));
        }

        return Mono.just(product)
                .map(p -> {
                    return Product.builder()
                            .id(null)
                            .sku(p.getSku())
                            .name(p.getName())
                            .description(p.getDescription())
                            .price(p.getPrice())
                            .categoryId(p.getCategoryId())
                            .brandId(p.getBrandId())
                            .stock(p.getStock())
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .features(p.getFeatures())
                            .build();
                })
                .flatMap(productRepositoryPort::save);
    }
}