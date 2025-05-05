package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.port.in.DeleteProductPort;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DeleteProductUseCaseImpl implements DeleteProductPort {

    private final ProductRepositoryPort productRepositoryPort;

    public DeleteProductUseCaseImpl(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<Void> deleteProductById(UUID id) {
        return productRepositoryPort.deleteById(id.toString())
                .onErrorMap(ex -> new RuntimeException("Failed to delete product with ID: " + id, ex));
    }
}