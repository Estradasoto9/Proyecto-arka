package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.application.dto.CreateProductRequestDTO;
import com.projectArka.product_service.application.dto.ProductResponseDTO;
import com.projectArka.product_service.application.dto.UpdateProductRequestDTO;
import com.projectArka.product_service.application.mapper.ProductMapper;
import com.projectArka.product_service.domain.exception.ProductAlreadyExistsException;
import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operations related to products")
public class ProductController {

    private final CreateProductPort createProductPort;
    private final GetProductPort getProductPort;
    private final UpdateProductPort updateProductPort;
    private final DeleteProductPort deleteProductPort;
    private final ProductMapper productMapper;

    public ProductController(CreateProductPort createProductPort,
                             GetProductPort getProductPort,
                             UpdateProductPort updateProductPort,
                             DeleteProductPort deleteProductPort,
                             ProductMapper productMapper) {
        this.createProductPort = createProductPort;
        this.getProductPort = getProductPort;
        this.updateProductPort = updateProductPort;
        this.deleteProductPort = deleteProductPort;
        this.productMapper = productMapper;
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details.")
    @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createProduct(@Valid @RequestBody CreateProductRequestDTO requestDTO) {
        Product product = productMapper.toEntity(requestDTO);
        return createProductPort.createProduct(product)
                .map(createdProduct -> ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Product created")))
                .onErrorResume(ProductAlreadyExistsException.class, e -> {
                    String message;
                    if (e.getField().equals("name")) {
                        message = "A product with the name: " + requestDTO.getName() + " already exists";
                    } else {
                        message = "A product with the SKU: " + requestDTO.getSku() + " already exists";
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message)));
                });
    }

    @Operation(summary = "Get a product by ID", description = "Retrieves a product based on its ID.")
    @ApiResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getProductById(@Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "ID of the product to retrieve") @PathVariable String id) {
        return parseUUID(id)
                .flatMap(getProductPort::getProductById)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(productMapper.toDTO(product)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get a product by SKU", description = "Retrieves a product based on its SKU.")
    @ApiResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/sku/{sku}")
    public Mono<ResponseEntity<?>> getProductBySku(@Parameter(in = ParameterIn.PATH, name = "sku", required = true, description = "SKU of the product to retrieve") @PathVariable String sku) {
        return getProductPort.getProductBySku(sku)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", "No product found with the SKU: " + sku))
                );
    }

    @Operation(summary = "Get a product by name", description = "Retrieves a product based on its name.")
    @ApiResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/name/{name}")
    public Mono<ResponseEntity<ProductResponseDTO>> getProductByName(@Parameter(in = ParameterIn.PATH, name = "name", required = true, description = "Name of the product to retrieve") @PathVariable String name) {
        return getProductPort.getProductByName(name)
                .map(productMapper::toDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all products", description = "Retrieves a list of all products.")
    @ApiResponse(responseCode = "200", description = "List of products", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class, type = "array")))
    @GetMapping
    public Flux<ProductResponseDTO> getAllProducts() {
        return getProductPort.getAllProducts()
                .map(productMapper::toDTO);
    }

    @Operation(summary = "Update a product", description = "Updates an existing product based on its ID.")
    @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProduct(@PathVariable String id, @Valid @RequestBody UpdateProductRequestDTO updateRequestDTO) {
        return parseUUID(id)
                .flatMap(uuid -> getProductPort.getProductById(uuid)
                        .flatMap(existingProduct -> {
                            Product productToUpdate = productMapper.toEntity(updateRequestDTO);
                            productToUpdate.setId(uuid.toString()); // Ensure the ID is from the URL
                            return updateProductPort.updateProduct(productToUpdate)
                                    .map(updatedProduct -> ResponseEntity.ok(productMapper.toDTO(updatedProduct)));
                        })
                        .defaultIfEmpty(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Delete a product by ID", description = "Deletes a product based on its ID.")
    @ApiResponse(responseCode = "200", description = "Product deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Product deleted\"}")))
    @ApiResponse(responseCode = "404", description = "Product not found")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteProductById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getProductPort.getProductById(uuid)
                        .flatMap(existingProduct -> deleteProductPort.deleteProductById(uuid)
                                .thenReturn(ResponseEntity.ok(Map.of("message", "Product deleted")))
                                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("message", "Product not found or error during deletion")))))
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Product not found with the ID: " + uuid))))
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException rse && rse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Invalid ID. Must be a UUID.")));
                    }
                    return Mono.error(e);
                });
    }

    private Mono<UUID> parseUUID(String id) {
        try {
            return Mono.just(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID. Must be a UUID."));
        }
    }
}