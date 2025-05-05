package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.GetBrandPort;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetBrandUseCaseImpl implements GetBrandPort {

    private final BrandRepositoryPort brandRepositoryPort;

    public GetBrandUseCaseImpl(BrandRepositoryPort brandRepositoryPort) {
        this.brandRepositoryPort = brandRepositoryPort;
    }

    @Override
    public Mono<Brand> getBrandById(UUID id) {
        return brandRepositoryPort.findById(id.toString());
    }

    @Override
    public Mono<Brand> getBrandByName(String name) {
        return brandRepositoryPort.findByName(name);
    }

    @Override
    public Flux<Brand> getAllBrands() {
        return brandRepositoryPort.findAll();
    }
}
