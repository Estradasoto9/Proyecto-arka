package com.projectArka.product_service.infrastructure.adapter.out.r2dbc.adapter;

import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.model.ProductFeature;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository.ProductFeatureRepository;
import com.projectArka.product_service.infrastructure.adapter.out.r2dbc.repository.ProductRepository;
import com.projectArka.product_service.infrastructure.entity.ProductEntity;
import com.projectArka.product_service.infrastructure.entity.ProductFeatureEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class R2dbcProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductRepository productRepository;
    private final ProductFeatureRepository productFeatureRepository;

    public R2dbcProductRepositoryAdapter(ProductRepository productRepository, ProductFeatureRepository productFeatureRepository) {
        this.productRepository = productRepository;
        this.productFeatureRepository = productFeatureRepository;
    }

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity productEntity = ProductEntity.fromDomain(product);

        return productRepository.save(productEntity)
                .flatMap(savedEntity -> {
                    if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
                        Mono<Void> deleteExistingFeatures = Mono.empty();
                        if (savedEntity.getId() != null) {
                            deleteExistingFeatures = productFeatureRepository.deleteByProductId(savedEntity.getId());
                        }

                        return deleteExistingFeatures.thenMany(Flux.fromIterable(product.getFeatures()))
                                .map(feature -> ProductFeatureEntity.builder()
                                        .productId(savedEntity.getId())
                                        .name(feature.getName())
                                        .value(feature.getValue())
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build())
                                .flatMap(productFeatureRepository::save)
                                .then(productFeatureRepository.findByProductId(savedEntity.getId())
                                        .map(entity -> ProductFeature.builder()
                                                .name(entity.getName())
                                                .value(entity.getValue())
                                                .build())
                                        .collectList()
                                        .map(features -> {
                                            Product updatedProduct = savedEntity.toDomain();
                                            updatedProduct.setFeatures(features);
                                            return updatedProduct;
                                        }));
                    } else {
                        return Mono.just(savedEntity.toDomain()).map(p -> {
                            p.setFeatures(java.util.Collections.emptyList());
                            return p;
                        });
                    }
                });
    }

    @Override
    public Mono<Product> findById(String id) {
        UUID uuid = UUID.fromString(id);
        return productRepository.findById(uuid)
                .flatMap(productEntity ->
                        productFeatureRepository.findByProductId(productEntity.getId())
                                .map(entity -> ProductFeature.builder()
                                        .name(entity.getName())
                                        .value(entity.getValue())
                                        .build())
                                .collectList()
                                .map(features -> {
                                    Product product = productEntity.toDomain();
                                    product.setFeatures(features);
                                    return product;
                                })
                );
    }

    @Override
    public Mono<Product> findBySku(String sku) {
        return productRepository.findBySku(sku)
                .flatMap(productEntity ->
                        productFeatureRepository.findByProductId(productEntity.getId())
                                .map(entity -> ProductFeature.builder()
                                        .name(entity.getName())
                                        .value(entity.getValue())
                                        .build())
                                .collectList()
                                .map(features -> {
                                    Product product = productEntity.toDomain();
                                    product.setFeatures(features);
                                    return product;
                                })
                );
    }

    @Override
    public Mono<Product> findByName(String name) {
        return productRepository.findByName(name)
                .flatMap(productEntity -> {
                    if (productEntity == null) {
                        return Mono.empty();
                    }
                    return productFeatureRepository.findByProductId(productEntity.getId())
                            .map(entity -> ProductFeature.builder()
                                    .name(entity.getName())
                                    .value(entity.getValue())
                                    .build())
                            .collectList()
                            .map(features -> {
                                Product product = productEntity.toDomain();
                                product.setFeatures(features);
                                return product;
                            });
                });
    }

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll()
                .flatMap(productEntity -> productFeatureRepository.findByProductId(productEntity.getId())
                        .map(entity -> ProductFeature.builder()
                                .name(entity.getName())
                                .value(entity.getValue())
                                .build())
                        .collectList()
                        .map(features -> {
                            Product product = productEntity.toDomain();
                            product.setFeatures(features);
                            return product;
                        })
                );
    }

    @Override
    public Mono<Void> deleteById(String id) {
        UUID uuid = UUID.fromString(id);
        return productRepository.deleteById(uuid);
    }

}