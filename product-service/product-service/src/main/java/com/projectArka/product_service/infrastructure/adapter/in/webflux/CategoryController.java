package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.application.dto.CategoryResponseDTO;
import com.projectArka.product_service.application.dto.CreateCategoryRequestDTO;
import com.projectArka.product_service.application.dto.UpdateCategoryRequestDTO;
import com.projectArka.product_service.application.mapper.CategoryMapper;
import com.projectArka.product_service.domain.exception.CategoryAlreadyExistsException;
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
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Operations related to categories")
public class CategoryController {

    private final CreateCategoryPort createCategoryPort;
    private final GetCategoryPort getCategoryPort;
    private final UpdateCategoryPort updateCategoryPort;
    private final DeleteCategoryPort deleteCategoryPort;
    private final CategoryMapper categoryMapper;

    public CategoryController(CreateCategoryPort createCategoryPort,
                              GetCategoryPort getCategoryPort,
                              UpdateCategoryPort updateCategoryPort,
                              DeleteCategoryPort deleteCategoryPort,
                              CategoryMapper categoryMapper) {
        this.createCategoryPort = createCategoryPort;
        this.getCategoryPort = getCategoryPort;
        this.updateCategoryPort = updateCategoryPort;
        this.deleteCategoryPort = deleteCategoryPort;
        this.categoryMapper = categoryMapper;
    }

    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details.")
    @ApiResponse(responseCode = "201", description = "Category created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createCategory(@Valid @RequestBody CreateCategoryRequestDTO requestDTO) {
        return createCategoryPort.createCategory(categoryMapper.toEntity(requestDTO))
                .map(createdCategory -> ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Category created")))
                .onErrorResume(CategoryAlreadyExistsException.class, e -> {
                    String message = "A category with the name: " + requestDTO.getName() + " already exists.";
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message)));
                });
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves a category based on its ID.")
    @ApiResponse(responseCode = "200", description = "Category found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CategoryResponseDTO>> getCategoryById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getCategoryPort.getCategoryById(uuid)
                        .map(categoryMapper::toDTO)
                        .map(ResponseEntity::ok)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + id))));
    }

    @Operation(summary = "Get all categories", description = "Retrieves a list of all categories.")
    @ApiResponse(responseCode = "200", description = "List of categories",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class, type = "array")))
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping
    public Flux<CategoryResponseDTO> getAllCategories() {
        return getCategoryPort.getAllCategories()
                .map(categoryMapper::toDTO);
    }

    @Operation(summary = "Search category by name", description = "Searches for a category by its name.")
    @ApiResponse(responseCode = "200", description = "Category found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Category not found")
    @GetMapping("/search")
    public Mono<ResponseEntity<CategoryResponseDTO>> getCategoryByName(@RequestParam String name) {
        return getCategoryPort.getCategoryByName(name) // Using getCategoryPort
                .map(categoryMapper::toDTO)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No category found with the name: " + name)));
    }

    @Operation(summary = "Update a category", description = "Updates an existing category based on its ID.")
    @ApiResponse(responseCode = "200", description = "Category updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CategoryResponseDTO>> updateCategory(@PathVariable String id, @Valid @RequestBody UpdateCategoryRequestDTO updateRequestDTO) {
        return parseUUID(id)
                .flatMap(uuid -> getCategoryPort.getCategoryById(uuid)
                        .flatMap(existingCategory -> {
                            categoryMapper.updateEntity(updateRequestDTO, existingCategory);
                            return updateCategoryPort.updateCategory(existingCategory)
                                    .map(categoryMapper::toDTO)
                                    .map(ResponseEntity::ok);
                        })
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + uuid))));
    }

    @Operation(summary = "Delete a category by ID", description = "Deletes a category based on its ID.")
    @ApiResponse(responseCode = "200", description = "Category deleted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Category deleted successfully\"}")))
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteCategoryById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getCategoryPort.getCategoryById(uuid)
                        .flatMap(existingCategory -> deleteCategoryPort.deleteCategoryById(uuid)
                                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Category deleted successfully"))))
                        )
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + uuid)))
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Invalid ID. Must be a valid UUID")));
                    }
                    return Mono.error(e);
                });
    }

    private Mono<UUID> parseUUID(String id) {
        try {
            return Mono.just(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID. Must be a valid UUID"));
        }
    }
}