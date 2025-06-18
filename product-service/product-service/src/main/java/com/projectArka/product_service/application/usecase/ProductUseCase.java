package com.projectArka.product_service.application.usecase;

import com.projectArka.product_service.domain.exception.ProductAlreadyExistsException;
import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.ICreateProductPort;
import com.projectArka.product_service.domain.port.in.IDeleteProductPort;
import com.projectArka.product_service.domain.port.in.IGetProductPort;
import com.projectArka.product_service.domain.port.in.IUpdateProductPort;
import com.projectArka.product_service.domain.port.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductUseCaseIIII implements ICreateProductPort, IGetProductPort, IUpdateProductPort, IDeleteProductPort {

    private final ProductRepositoryPort productRepositoryPort;

    public ProductUseCaseIIII(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<Product> createProduct(Product product) {
        if (product.getId() != null) {
            return Mono.error(new IllegalArgumentException("New product must not have ID"));
        }

        return productRepositoryPort.findByName(product.getName())
                .flatMap(existing -> Mono.<Product>error(new ProductAlreadyExistsException(
                        "Product with the name already exists: " + product.getName(), "name"
                )))
                .switchIfEmpty(
                        productRepositoryPort.findBySku(product.getSku())
                                .flatMap(existing -> Mono.<Product>error(new ProductAlreadyExistsException(
                                        "Product with the SKU already exists: " + product.getSku(), "sku"
                                )))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    Product newProduct = Product.builder()
                            .sku(product.getSku())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .categoryId(product.getCategoryId())
                            .brandId(product.getBrandId())
                            .stock(product.getStock())
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .features(product.getFeatures())
                            .build();
                    return productRepositoryPort.save(newProduct);
                }));
    }

    @Override
    public Mono<Product> getProductById(UUID id) {
        return productRepositoryPort.findById(id.toString());
    }

    @Override
    public Mono<Product> getProductBySku(String sku) {
        return productRepositoryPort.findBySku(sku);
    }

    @Override
    public Mono<Product> getProductByName(String name) {
        return productRepositoryPort.findByName(name);
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }

    @Override
    public Mono<Product> updateProduct(Product product) {
        return productRepositoryPort.save(product);
    }

    @Override
    public Mono<Void> deleteProductById(UUID id) {
        return productRepositoryPort.deleteById(id.toString())
                .onErrorMap(ex -> new RuntimeException("Failed to delete product with ID: " + id, ex));
    }
}