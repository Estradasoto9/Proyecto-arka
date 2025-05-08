package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.exception.BrandAlreadyExistsException;
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
            return Mono.error(new IllegalArgumentException("The new brand must not have an ID"));
        }

        return brandRepositoryPort.findByName(brand.getName())
                .flatMap(existingBrand ->
                        Mono.<Brand>error(new BrandAlreadyExistsException("There is already a brand with the name: " + brand.getName()))
                )
                .switchIfEmpty(
                        Mono.defer(() -> brandRepositoryPort.save(Brand.builder()
                                .name(brand.getName())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()))
                );
    }
}
