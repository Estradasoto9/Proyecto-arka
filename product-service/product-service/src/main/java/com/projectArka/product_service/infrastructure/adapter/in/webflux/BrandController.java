package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.domain.model.Brand;
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
@RequestMapping("/api/brands")
@Tag(name = "Brands", description = "Operaciones relacionadas con las marcas")
public class BrandController {

    private final CreateBrandPort createBrandPort;
    private final GetBrandPort getBrandPort;
    private final UpdateBrandPort updateBrandPort;
    private final DeleteBrandPort deleteBrandPort;

    public BrandController(CreateBrandPort createBrandPort,
                           GetBrandPort getBrandPort,
                           UpdateBrandPort updateBrandPort,
                           DeleteBrandPort deleteBrandPort) {
        this.createBrandPort = createBrandPort;
        this.getBrandPort = getBrandPort;
        this.updateBrandPort = updateBrandPort;
        this.deleteBrandPort = deleteBrandPort;
    }

    @Operation(summary = "Crear una nueva marca", description = "Crea una nueva marca con los detalles proporcionados.")
    @ApiResponse(responseCode = "201", description = "Marca creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Brand.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @PostMapping
    public Mono<ResponseEntity<Brand>> createBrand(@RequestBody Brand brand) {
        return createBrandPort.createBrand(brand)
                .map(createdBrand -> ResponseEntity.status(HttpStatus.CREATED).body(createdBrand));
    }


    @Operation(summary = "Obtener una marca por ID", description = "Obtiene una marca basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Marca encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Brand.class)))
    @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Brand>> getBrandById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getBrandPort.getBrandById(uuid)
                        .map(ResponseEntity::ok)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Marca no encontrada"
                        )))
                );
    }



    @Operation(summary = "Obtener todas las marcas", description = "Obtiene una lista de todas las marcas.")
    @ApiResponse(responseCode = "200", description = "Lista de marcas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Brand.class, type = "array")))
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping
    public Flux<Brand> getAllBrands() {
        return getBrandPort.getAllBrands();
    }

    @Operation(summary = "Buscar una marca por nombre", description = "Busca una marca por su nombre.")
    @ApiResponse(responseCode = "200", description = "Marca encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Brand.class)))
    @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    @GetMapping("/search")
    public Mono<ResponseEntity<Brand>> getBrandByName(@RequestParam String name) {
        return getBrandPort.getBrandByName(name)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró ninguna marca con el nombre: " + name)));
    }


    @Operation(summary = "Actualizar una marca", description = "Actualiza una marca existente basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Marca actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Brand.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Brand>> updateBrand(@PathVariable String id, @RequestBody Brand brand) {
        return parseUUID(id)
                .flatMap(uuid -> {
                    brand.setId(uuid.toString());
                    return updateBrandPort.updateBrand(brand)
                            .map(ResponseEntity::ok)
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                });
    }

    @Operation(summary = "Eliminar una marca por ID", description = "Elimina una marca basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Marca eliminada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Marca eliminada\"}")))
    @ApiResponse(responseCode = "404", description = "Marca no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteBrandById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getBrandPort.getBrandById(uuid) // Verificar si la marca existe
                        .flatMap(existingBrand -> deleteBrandPort.deleteBrandById(uuid)
                                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Marca eliminada"))))
                        )
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con el ID: " + uuid)))
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "ID no válido. Debe ser un UUID.")));
                    }
                    return Mono.error(e); // Propaga otros errores
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