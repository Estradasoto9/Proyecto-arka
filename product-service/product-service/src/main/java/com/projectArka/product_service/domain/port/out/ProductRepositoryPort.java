package com.projectArka.product_service.domain.port.out;

import com.projectArka.product_service.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryPort {
    Mono<Product> save(Product product);
    Mono<Product> findById(String id);
    Mono<Product> findBySku(String sku);
    Flux<Product> findAll();
    Mono<Void> deleteById(String id);
}