package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.CreateBrandPort;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
public class CreateBrandUseCaseImpl implements CreateBrandPort {

    private final BrandRepositoryPort brandRepositoryPort;

    public CreateBrandUseCaseImpl(BrandRepositoryPort brandRepositoryPort) {
        this.brandRepositoryPort = brandRepositoryPort;
    }

    @Override
    public Mono<Brand> createBrand(Brand brand) {
        if (brand.getId() != null) {
            return Mono.error(new IllegalArgumentException("New brand must not have an ID"));
        }

        return Mono.just(brand)
                .map(b -> {
                    Brand newBrand = Brand.builder()
                            .id(null)
                            .name(b.getName())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return newBrand;

                })
                .flatMap(brandRepositoryPort::save);
    }
}