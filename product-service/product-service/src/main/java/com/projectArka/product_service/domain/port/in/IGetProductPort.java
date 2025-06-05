package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetProductPort {
    Mono<Product> getProductById(UUID id);
    Mono<Product> getProductBySku(String sku);
    Mono<Product> getProductByName(String name);
    Flux<Product> getAllProducts();
}