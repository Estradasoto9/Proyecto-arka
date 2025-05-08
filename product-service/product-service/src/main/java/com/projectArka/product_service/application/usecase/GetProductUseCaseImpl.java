package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.GetProductPort;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetProductUseCaseImpl implements GetProductPort {

    private final ProductRepositoryPort productRepositoryPort;

    public GetProductUseCaseImpl(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<Product> getProductById(UUID id) {
        return productRepositoryPort.findById(id.toString());
    }

    @Override
    public Mono<Product> getProductBySku(String sku) {
        return productRepositoryPort.findBySku(sku);
    }

    @Override
    public Mono<Product> getProductByName(String name) {
        return productRepositoryPort.findByName(name);
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }
}