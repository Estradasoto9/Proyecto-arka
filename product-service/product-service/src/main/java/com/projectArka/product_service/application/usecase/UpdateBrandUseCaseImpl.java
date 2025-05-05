package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.UpdateBrandPort;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateBrandUseCaseImpl implements UpdateBrandPort {

    private final BrandRepositoryPort brandRepositoryPort;

    public UpdateBrandUseCaseImpl(BrandRepositoryPort brandRepositoryPort) {
        this.brandRepositoryPort = brandRepositoryPort;
    }

    @Override
    public Mono<Brand> updateBrand(Brand brand) {
        return brandRepositoryPort.save(brand);
    }
}