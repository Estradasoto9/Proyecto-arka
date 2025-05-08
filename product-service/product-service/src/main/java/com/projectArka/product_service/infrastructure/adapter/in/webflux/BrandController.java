package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.application.dto.BrandResponseDTO;
import com.projectArka.product_service.application.dto.CreateBrandRequestDTO;
import com.projectArka.product_service.application.dto.UpdateBrandRequestDTO;
import com.projectArka.product_service.application.mapper.BrandMapper;
import com.projectArka.product_service.domain.exception.BrandAlreadyExistsException;
import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/brands")
@Tag(name = "Brands", description = "Operations related to brands")
public class BrandController {

    private final CreateBrandPort createBrandPort;
    private final GetBrandPort getBrandPort;
    private final UpdateBrandPort updateBrandPort;
    private final DeleteBrandPort deleteBrandPort;
    private final BrandMapper brandMapper;

    public BrandController(CreateBrandPort createBrandPort,
                           GetBrandPort getBrandPort,
                           UpdateBrandPort updateBrandPort,
                           DeleteBrandPort deleteBrandPort,
                           BrandMapper brandMapper) {
        this.createBrandPort = createBrandPort;
        this.getBrandPort = getBrandPort;
        this.updateBrandPort = updateBrandPort;
        this.deleteBrandPort = deleteBrandPort;
        this.brandMapper = brandMapper;
    }

    @Operation(summary = "Create a new brand", description = "Creates a new brand with the provided details.")
    @ApiResponse(responseCode = "201", description = "Brand created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createBrand(@Valid @RequestBody CreateBrandRequestDTO requestDTO) {
        return createBrandPort.createBrand(brandMapper.toEntity(requestDTO))
                .map(createdBrand -> ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Brand created")))
                .onErrorResume(BrandAlreadyExistsException.class, e -> {
                    String message = "A brand with the name: " + requestDTO.getName() + " already exists.";
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message)));
                });
    }


    @Operation(summary = "Get a brand by ID", description = "Retrieves a brand based on its ID.")
    @ApiResponse(responseCode = "200", description = "Brand found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Brand not found")
    @GetMapping("/{id}")

    public Mono<ResponseEntity<Brand>> getBrandById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getBrandPort.getBrandById(uuid)
                        .map(ResponseEntity::ok)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found")))
                );
    }

    @Operation(summary = "Get all brands", description = "Retrieves a list of all brands.")
    @ApiResponse(responseCode = "200", description = "List of brands",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class, type = "array")))
    @GetMapping
    public Flux<BrandResponseDTO> getAllBrands() {
        return getBrandPort.getAllBrands()
                .map(brandMapper::toDTO);
    }

    @Operation(summary = "Search for a brand by name", description = "Searches for a brand by its name.")
    @ApiResponse(responseCode = "200", description = "Brand found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Brand not found")
    @GetMapping("/search")
    public Mono<ResponseEntity<Brand>> getBrandByName(@RequestParam String name) {
        return getBrandPort.getBrandByName(name)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No brand found with the name: " + name)));
    }

    @Operation(summary = "Update a brand", description = "Updates an existing brand based on its ID.")
    @ApiResponse(responseCode = "200", description = "Brand updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BrandResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Brand not found")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<BrandResponseDTO>> updateBrand(@PathVariable String id, @Valid @RequestBody UpdateBrandRequestDTO updateRequestDTO) {
        return parseUUID(id)
                .flatMap(uuid -> getBrandPort.getBrandById(uuid)
                        .flatMap(existingBrand -> {
                            brandMapper.updateEntity(updateRequestDTO, existingBrand);
                            return updateBrandPort.updateBrand(existingBrand)
                                    .map(brandMapper::toDTO)
                                    .map(ResponseEntity::ok);
                        })
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + uuid))));
    }

    @Operation(summary = "Delete a brand by ID", description = "Deletes a brand based on its ID.")
    @ApiResponse(responseCode = "200", description = "Brand deleted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Brand deleted\"}")))
    @ApiResponse(responseCode = "404", description = "Brand not found")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteBrandById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getBrandPort.getBrandById(uuid)
                        .flatMap(existingBrand -> deleteBrandPort.deleteBrandById(uuid)
                                .thenReturn(ResponseEntity.ok(Map.of("message", "Brand deleted"))))
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Brand not found with ID: " + uuid))))
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