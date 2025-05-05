package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.port.in.DeleteBrandPort;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DeleteBrandUseCaseImpl implements DeleteBrandPort {

    private final BrandRepositoryPort brandRepositoryPort;

    public DeleteBrandUseCaseImpl(BrandRepositoryPort brandRepositoryPort) {
        this.brandRepositoryPort = brandRepositoryPort;
    }

    @Override
    public Mono<Void> deleteBrandById(UUID id) {
        return brandRepositoryPort.deleteById(id.toString())
                .onErrorMap(ex ->
                        new RuntimeException("Failed to delete brand", ex)
                );
    }
}