package com.projectArka.product_service.domain.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DeleteBrandPort {
    Mono<Void> deleteBrandById(UUID id);
}