package com.projectArka.product_service.controllerTest;

import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.in.CreateCategoryPort;
import com.projectArka.product_service.domain.port.in.DeleteCategoryPort;
import com.projectArka.product_service.domain.port.in.GetCategoryPort;
import com.projectArka.product_service.domain.port.in.UpdateCategoryPort;
import com.projectArka.product_service.infrastructure.adapter.in.webflux.CategoryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CategoryControllerTest {

    private WebTestClient webTestClient;
    private CreateCategoryPort createCategoryPort;
    private GetCategoryPort getCategoryPort;
    private UpdateCategoryPort updateCategoryPort;
    private DeleteCategoryPort deleteCategoryPort;

    @BeforeEach
    void setup() {
        createCategoryPort = mock(CreateCategoryPort.class);
        getCategoryPort = mock(GetCategoryPort.class);
        updateCategoryPort = mock(UpdateCategoryPort.class);
        deleteCategoryPort = mock(DeleteCategoryPort.class);

        CategoryController controller = new CategoryController(
                createCategoryPort,
                getCategoryPort,
                updateCategoryPort,
                deleteCategoryPort
        );

        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateCategory() {
        Category newCategory = Category.create("Tecnología", "Productos tecnológicos");

        when(createCategoryPort.createCategory(any(Category.class)))
                .thenReturn(Mono.just(newCategory));

        webTestClient.post()
                .uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCategory)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Tecnología");
    }

    @Test
    void testGetCategoryById() {
        UUID id = UUID.randomUUID();
        Category category = Category.create("Moda", "Ropa y accesorios");
        category.setId(id.toString());

        when(getCategoryPort.getCategoryById(eq(id))).thenReturn(Mono.just(category));

        webTestClient.get()
                .uri("/api/categories/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Moda");
    }

    @Test
    void testGetAllCategories() {
        Category category1 = Category.create("Deportes", "Artículos deportivos");
        Category category2 = Category.create("Hogar", "Artículos para el hogar");

        when(getCategoryPort.getAllCategories()).thenReturn(Flux.just(category1, category2));

        webTestClient.get()
                .uri("/api/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Category.class)
                .hasSize(2);
    }

    @Test
    void testGetCategoryByName() {
        Category category = Category.create("Electrónica", "Dispositivos electrónicos");

        when(getCategoryPort.getCategoryByName("Electrónica")).thenReturn(Mono.just(category));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/categories/search")
                        .queryParam("name", "Electrónica").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.description").isEqualTo("Dispositivos electrónicos");
    }

    @Test
    void testUpdateCategory() {
        UUID id = UUID.randomUUID();
        Category updatedCategory = Category.create("Salud", "Productos de salud");
        updatedCategory.setId(id.toString());

        when(getCategoryPort.getCategoryById(id)).thenReturn(Mono.just(updatedCategory));
        when(updateCategoryPort.updateCategory(any(Category.class))).thenReturn(Mono.just(updatedCategory));

        webTestClient.put()
                .uri("/api/categories/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCategory)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Salud");
    }

    @Test
    void testDeleteCategoryById() {
        UUID id = UUID.randomUUID();
        Category category = Category.create("Libros", "Libros y literatura");
        category.setId(id.toString());

        when(getCategoryPort.getCategoryById(id)).thenReturn(Mono.just(category));
        when(deleteCategoryPort.deleteCategoryById(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/categories/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Categoría eliminada correctamente");
    }
}
