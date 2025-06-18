package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.exception.BrandAlreadyExistsException;
import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.ICreateBrandPort;
import com.projectArka.product_service.domain.port.in.IDeleteBrandPort;
import com.projectArka.product_service.domain.port.in.IGetBrandPort;
import com.projectArka.product_service.domain.port.in.IUpdateBrandPort;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BrandUseCaseIII implements ICreateBrandPort, IGetBrandPort, IUpdateBrandPort, IDeleteBrandPort {

    private final BrandRepositoryPort brandRepositoryPort;

    public BrandUseCaseIII(BrandRepositoryPort brandRepositoryPort) {
        this.brandRepositoryPort = brandRepositoryPort;
    }

    @Override
    public Mono<Brand> createBrand(Brand brand) {
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

    @Override
    public Mono<Brand> updateBrand(Brand brand) {
        return brandRepositoryPort.save(brand);
    }

    @Override
    public Mono<Void> deleteBrandById(UUID id) {
        return brandRepositoryPort.deleteById(id.toString())
                .onErrorMap(ex ->
                        new RuntimeException("Failed to delete brand", ex)
                );
    }
}