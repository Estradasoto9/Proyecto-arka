package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.UpdateProductPort;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateProductUseCaseImpl implements UpdateProductPort {

    private final ProductRepositoryPort productRepositoryPort;

    public UpdateProductUseCaseImpl(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<Product> updateProduct(Product product) {
        return productRepositoryPort.save(product);
    }
}