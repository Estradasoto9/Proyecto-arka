package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Products", description = "Operaciones relacionadas con los productos")
public class ProductController {

    private final CreateProductPort createProductPort;
    private final GetProductPort getProductPort;
    private final UpdateProductPort updateProductPort;
    private final DeleteProductPort deleteProductPort;

    public ProductController(CreateProductPort createProductPort,
                             GetProductPort getProductPort,
                             UpdateProductPort updateProductPort,
                             DeleteProductPort deleteProductPort) {
        this.createProductPort = createProductPort;
        this.getProductPort = getProductPort;
        this.updateProductPort = updateProductPort;
        this.deleteProductPort = deleteProductPort;
    }

    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto con los detalles proporcionados.")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @PostMapping
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return createProductPort.createProduct(product)
                .map(createdProduct -> ResponseEntity.status(HttpStatus.CREATED).body(createdProduct));
    }

    @Operation(summary = "Obtener un producto por ID", description = "Obtiene un producto basado en su ID.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> getProductById(@Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "ID del producto a obtener") @PathVariable String id) {
        return parseUUID(id)
                .flatMap(getProductPort::getProductById)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", "ID no válido"))
                );
    }

    @Operation(summary = "Obtener un producto por SKU", description = "Obtiene un producto basado en su SKU.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @GetMapping("/sku/{sku}")
    public Mono<ResponseEntity<?>> getProductBySku(@Parameter(in = ParameterIn.PATH, name = "sku", required = true, description = "SKU del producto a obtener") @PathVariable String sku) {
        return getProductPort.getProductBySku(sku)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", "No se encontró ningún producto con el SKU: " + sku))
                );
    }

    @Operation(summary = "Obtener todos los productos", description = "Obtiene una lista de todos los productos.")
    @ApiResponse(responseCode = "200", description = "Lista de productos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class, type = "array")))
    @GetMapping
    public Flux<Product> getAllProducts() {
        return getProductPort.getAllProducts();
    }

    @Operation(summary = "Actualizar un producto", description = "Actualiza un producto existente basado en su ID.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return parseUUID(id)
                .flatMap(uuid -> {
                    product.setId(uuid.toString());
                    return updateProductPort.updateProduct(product)
                            .map(ResponseEntity::ok)
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                });
    }

    @Operation(summary = "Eliminar un producto por ID", description = "Elimina un producto basado en su ID.")
    @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Producto eliminado\"}")))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteProductById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getProductPort.getProductById(uuid)
                        .flatMap(existingProduct -> deleteProductPort.deleteProductById(uuid)
                                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Producto eliminado"))))
                                .onErrorResume(ex -> Mono.just(ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("message", "Producto no encontrado o error al eliminar"))))
                        )
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con el ID: " + uuid)))
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException rse && rse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "ID no válido. Debe ser un UUID.")));
                    }
                    return Mono.error(e);
                });
    }



    private Mono<UUID> parseUUID(String id) {
        try {
            return Mono.just(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID no válido. Debe ser un UUID."));
        }
    }
}