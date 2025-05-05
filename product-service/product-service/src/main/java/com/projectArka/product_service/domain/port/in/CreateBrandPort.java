package com.projectArka.product_service.domain.port.in;

import com.projectArka.product_service.domain.model.Brand;
import reactor.core.publisher.Mono;

public interface CreateBrandPort {
    Mono<Brand> createBrand(Brand brand);
}