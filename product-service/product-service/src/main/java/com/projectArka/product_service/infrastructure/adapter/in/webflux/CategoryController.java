package com.projectArka.product_service.infrastructure.adapter.in.webflux;

import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.CreateCategoryPort;
import com.projectArka.product_service.domain.port.in.DeleteCategoryPort;
import com.projectArka.product_service.domain.port.in.GetCategoryPort;
import com.projectArka.product_service.domain.port.in.UpdateCategoryPort;
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
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Operaciones relacionadas con las categorías")
public class CategoryController {

    private final CreateCategoryPort createCategoryPort;
    private final GetCategoryPort getCategoryPort;
    private final UpdateCategoryPort updateCategoryPort;
    private final DeleteCategoryPort deleteCategoryPort;

    public CategoryController(CreateCategoryPort createCategoryPort,
                              GetCategoryPort getCategoryPort,
                              UpdateCategoryPort updateCategoryPort,
                              DeleteCategoryPort deleteCategoryPort) {
        this.createCategoryPort = createCategoryPort;
        this.getCategoryPort = getCategoryPort;
        this.updateCategoryPort = updateCategoryPort;
        this.deleteCategoryPort = deleteCategoryPort;
    }

    @Operation(summary = "Crear una nueva categoría", description = "Crea una nueva categoría con los detalles proporcionados.")
    @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @PostMapping
    public Mono<ResponseEntity<Category>> createCategory(@RequestBody Category category) {
        return createCategoryPort.createCategory(category)
                .map(createdCategory -> ResponseEntity.status(HttpStatus.CREATED).body(createdCategory));
    }

    @Operation(summary = "Obtener una categoría por ID", description = "Obtiene una categoría basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class)))
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Category>> getCategoryById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(getCategoryPort::getCategoryById)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada con el ID: " + id)));
    }

    @Operation(summary = "Obtener todas las categorías", description = "Obtiene una lista de todas las categorías.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class, type = "array")))
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping
    public Flux<Category> getAllCategories() {
        return getCategoryPort.getAllCategories();
    }

    @Operation(summary = "Buscar categoría por nombre", description = "Busca una categoría por su nombre.")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class)))
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @GetMapping("/search")
    public Mono<ResponseEntity<Category>> getCategoryByName(@RequestParam String name) {
        return getCategoryPort.getCategoryByName(name)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró ninguna categoría con el nombre: " + name)));
    }

    @Operation(summary = "Actualizar una categoría", description = "Actualiza una categoría existente basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Category>> updateCategory(@PathVariable String id, @RequestBody Category category) {
        return parseUUID(id)
                .flatMap(uuid -> {
                    category.setId(id);
                    return updateCategoryPort.updateCategory(category)
                            .map(ResponseEntity::ok)
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                });
    }

    @Operation(summary = "Eliminar una categoría por ID", description = "Elimina una categoría basada en su ID.")
    @ApiResponse(responseCode = "200", description = "Categoría eliminada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Categoría eliminada correctamente\"}")))
    @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> deleteCategoryById(@PathVariable String id) {
        return parseUUID(id)
                .flatMap(uuid -> getCategoryPort.getCategoryById(uuid) // Verificar si la categoría existe
                        .flatMap(existingCategory -> deleteCategoryPort.deleteCategoryById(uuid)
                                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Categoría eliminada correctamente"))))
                        )
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada con el ID: " + uuid)))
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException && ((ResponseStatusException) e).getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "ID no válido. Debe ser un UUID.")));
                    }
                    return Mono.error(e);
                });
    }

    private Mono<UUID> parseUUID(String id) {
        try {
            return Mono.just(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID debe ser un UUID válido"));
        }
    }
}