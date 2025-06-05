package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Brand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetBrandPort {
    Mono<Brand> getBrandById(UUID id);
    Mono<Brand> getBrandByName(String name);
    Flux<Brand> getAllBrands();
}