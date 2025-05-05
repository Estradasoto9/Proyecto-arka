package com.projectArka.product_service.controllerTest;


import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.port.in.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = com.projectArka.product_service.infrastructure.adapter.in.webflux.ProductController.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CreateProductPort createProductPort;

    @MockBean
    private GetProductPort getProductPort;

    @MockBean
    private UpdateProductPort updateProductPort;

    @MockBean
    private DeleteProductPort deleteProductPort;

    private Product sampleProduct;

    @BeforeEach
    void setup() {
        sampleProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .categoryId(UUID.randomUUID())
                .brandId(UUID.randomUUID())
                .stock(10)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Crear producto exitosamente")
    void testCreateProduct() {
        when(createProductPort.createProduct(any(Product.class))).thenReturn(Mono.just(sampleProduct));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(sampleProduct);
    }

    @Test
    @DisplayName("Obtener producto por ID - encontrado")
    void testGetProductByIdFound() {
        when(getProductPort.getProductById(any(UUID.class))).thenReturn(Mono.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products/{id}", sampleProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(sampleProduct);
    }

    @Test
    @DisplayName("Obtener producto por ID - no encontrado")
    void testGetProductByIdNotFound() {
        when(getProductPort.getProductById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/{id}", UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Producto no encontrado"));
    }

    @Test
    @DisplayName("Obtener producto por SKU - encontrado")
    void testGetProductBySkuFound() {
        when(getProductPort.getProductBySku("SKU-001")).thenReturn(Mono.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products/sku/{sku}", "SKU-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(sampleProduct);
    }

    @Test
    @DisplayName("Obtener producto por SKU - no encontrado")
    void testGetProductBySkuNotFound() {
        when(getProductPort.getProductBySku("SKU-999")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/sku/{sku}", "SKU-999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Producto no encontrado"));
    }

    @Test
    @DisplayName("Obtener todos los productos")
    void testGetAllProducts() {
        when(getProductPort.getAllProducts()).thenReturn(Flux.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("Actualizar producto - exitoso")
    void testUpdateProductFound() {
        when(updateProductPort.updateProduct(any(Product.class))).thenReturn(Mono.just(sampleProduct));

        webTestClient.put()
                .uri("/api/products/{id}", sampleProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(sampleProduct);
    }

    @Test
    @DisplayName("Actualizar producto - no encontrado")
    void testUpdateProductNotFound() {
        when(updateProductPort.updateProduct(any(Product.class))).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/products/{id}", sampleProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleProduct)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Eliminar producto - exitoso")
    void testDeleteProductByIdSuccess() {
        UUID id = UUID.fromString(sampleProduct.getId());

        when(getProductPort.getProductById(id)).thenReturn(Mono.just(sampleProduct));
        when(deleteProductPort.deleteProductById(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/products/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(map -> org.assertj.core.api.Assertions.assertThat(map.get("message")).isEqualTo("Producto eliminado"));
    }

    @Test
    @DisplayName("Eliminar producto - error o no encontrado")
    void testDeleteProductByIdError() {
        UUID randomId = UUID.randomUUID();

        when(getProductPort.getProductById(randomId)).thenReturn(Mono.just(sampleProduct));
        when(deleteProductPort.deleteProductById(randomId)).thenReturn(Mono.error(new RuntimeException("No encontrado")));

        webTestClient.delete()
                .uri("/api/products/{id}", randomId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Producto no encontrado o error al eliminar");
    }


    @Test
    @DisplayName("Crear producto - error de validación")
    void testCreateProductInvalid() {
        Product invalidProduct = new Product();

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidProduct)
                .exchange()
                .expectStatus().is5xxServerError(); // o BAD_REQUEST si manejas validaciones
    }

    @Test
    @DisplayName("Obtener producto con UUID inválido")
    void testGetProductByInvalidUUID() {
        webTestClient.get()
                .uri("/api/products/{id}", "not-a-valid-uuid")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Actualizar producto con UUID inválido")
    void testUpdateProductInvalidUUID() {
        webTestClient.put()
                .uri("/api/products/{id}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleProduct)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Eliminar producto con UUID inválido")
    void testDeleteProductInvalidUUID() {
        webTestClient.delete()
                .uri("/api/products/{id}", "invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
