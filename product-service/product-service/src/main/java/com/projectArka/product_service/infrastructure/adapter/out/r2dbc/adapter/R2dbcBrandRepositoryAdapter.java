package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.adapter;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.out.BrandRepositoryPort;
import com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository.BrandRepository;
import com.projectArka.product_service.infrastructure.entity.BrandEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class R2dbcBrandRepositoryAdapter implements BrandRepositoryPort {

    private final BrandRepository brandRepository;

    public R2dbcBrandRepositoryAdapter(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public Mono<Brand> save(Brand brand) {
        BrandEntity brandEntity = BrandEntity.fromDomain(brand);
        return Mono.just(brand)
                .map(BrandEntity::fromDomain)
                .flatMap(brandRepository::save)
                .map(BrandEntity::toDomain);
    }

    @Override
    public Mono<Brand> findById(String id) {
        UUID uuid = UUID.fromString(id);
        return brandRepository.findById(uuid)
                .map(BrandEntity::toDomain);
    }

    @Override
    public Mono<Brand> findByName(String name) {
        return brandRepository.findByName(name)
                .map(BrandEntity::toDomain);
    }

    @Override
    public Flux<Brand> findAll() {
        return brandRepository.findAll()
                .map(BrandEntity::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        UUID uuid = UUID.fromString(id);
        return brandRepository.deleteById(uuid);
    }
}
