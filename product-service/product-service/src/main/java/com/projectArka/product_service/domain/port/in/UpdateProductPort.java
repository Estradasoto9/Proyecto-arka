package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Product;
import reactor.core.publisher.Mono;

public interface UpdateProductPort {
    Mono<Product> updateProduct(Product product);
}