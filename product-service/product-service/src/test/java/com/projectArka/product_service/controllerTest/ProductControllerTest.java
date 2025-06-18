package com.projectArka.product_service.controllerTest;

import com.projectArka.product_service.application.dto.CreateProductRequestDTO;
import com.projectArka.product_service.application.dto.ProductResponseDTO;
import com.projectArka.product_service.application.dto.UpdateProductRequestDTO;
import com.projectArka.product_service.application.mapper.ProductMapper;
import com.projectArka.product_service.domain.model.Product;
import com.projectArka.product_service.domain.exception.ProductAlreadyExistsException;
import com.projectArka.product_service.domain.port.in.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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

    @MockBean
    private ProductMapper productMapper;

    private Product sampleProduct;
    private ProductResponseDTO sampleProductDTO;
    private CreateProductRequestDTO createRequestDTO;
    private UpdateProductRequestDTO updateRequestDTO;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleProduct = Product.builder()
                .id(productId.toString())
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .categoryId(categoryId)
                .brandId(brandId)
                .stock(10)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleProductDTO = ProductResponseDTO.builder()
                .id(productId.toString())
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .categoryId(UUID.fromString(categoryId.toString()))
                .brandId(UUID.fromString(brandId.toString()))
                .stock(10)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createRequestDTO = CreateProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .categoryId(UUID.fromString(categoryId.toString()))
                .brandId(UUID.fromString(brandId.toString()))
                .stock(10)
                .build();

        updateRequestDTO = UpdateProductRequestDTO.builder()
                .name("Updated Product")
                .price(BigDecimal.valueOf(120.00))
                .stock(15)
                .active(false)
                .build();

        when(productMapper.toEntity(any(CreateProductRequestDTO.class))).thenReturn(sampleProduct);
        when(productMapper.toDTO(any(Product.class))).thenReturn(sampleProductDTO);
        when(productMapper.toEntity(any(UpdateProductRequestDTO.class))).thenReturn(sampleProduct);
    }

    @Test
    @DisplayName("Successfully create product")
    void testCreateProduct() {
        when(createProductPort.createProduct(any(Product.class))).thenReturn(Mono.just(sampleProduct));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Product created"));
    }

    @Test
    @DisplayName("Create product - product with same name already exists")
    void testCreateProduct_nameAlreadyExists() {
        when(createProductPort.createProduct(any(Product.class)))
                .thenReturn(Mono.error(new ProductAlreadyExistsException("name", createRequestDTO.getName())));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("A product with the name: Test Product already exists"));
    }

    @Test
    @DisplayName("Create product - product with same SKU already exists")
    void testCreateProduct_skuAlreadyExists() {
        when(createProductPort.createProduct(any(Product.class)))
                .thenReturn(Mono.error(new ProductAlreadyExistsException("sku", createRequestDTO.getSku())));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("A product with the SKU: SKU-001 already exists"));
    }

    @Test
    @DisplayName("Get product by ID - found")
    void testGetProductByIdFound() {
        when(getProductPort.getProductById(UUID.fromString(sampleProduct.getId()))).thenReturn(Mono.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products/{id}", sampleProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(sampleProductDTO);
    }

    @Test
    @DisplayName("Get product by ID - invalid ID")
    void testGetProductByIdInvalidId() {
        webTestClient.get()
                .uri("/api/products/invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Invalid ID. Must be a UUID."));
    }

    @Test
    @DisplayName("Get product by ID - not found")
    void testGetProductByIdNotFound() {
        when(getProductPort.getProductById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/{id}", UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Get product by SKU - found")
    void testGetProductBySkuFound() {
        when(getProductPort.getProductBySku("SKU-001")).thenReturn(Mono.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products/sku/{sku}", "SKU-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(sampleProductDTO);
    }

    @Test
    @DisplayName("Get product by SKU - not found")
    void testGetProductBySkuNotFound() {
        when(getProductPort.getProductBySku("SKU-999")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/sku/{sku}", "SKU-999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("No product found with the SKU: SKU-999"));
    }

    @Test
    @DisplayName("Get product by name - found")
    void testGetProductByNameFound() {
        when(getProductPort.getProductByName("Test Product")).thenReturn(Mono.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products/name/{name}", "Test Product")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(sampleProductDTO);
    }

    @Test
    @DisplayName("Get product by name - not found")
    void testGetProductByNameNotFound() {
        when(getProductPort.getProductByName("NonExistent Product")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/name/{name}", "NonExistent Product")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Get all products")
    void testGetAllProducts() {
        when(getProductPort.getAllProducts()).thenReturn(Flux.just(sampleProduct));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(1)
                .contains(sampleProductDTO);
    }

    @Test
    @DisplayName("Update product - successful")
    void testUpdateProductFound() {
        when(getProductPort.getProductById(UUID.fromString(sampleProduct.getId()))).thenReturn(Mono.just(sampleProduct));
        when(updateProductPort.updateProduct(any(Product.class))).thenReturn(Mono.just(sampleProduct));

        webTestClient.put()
                .uri("/api/products/{id}", sampleProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(sampleProductDTO);
    }

    @Test
    @DisplayName("Update product - invalid ID")
    void testUpdateProductInvalidId() {
        webTestClient.put()
                .uri("/api/products/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequestDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Invalid ID. Must be a UUID."));
    }

    @Test
    @DisplayName("Update product - not found")
    void testUpdateProductNotFound() {
        when(getProductPort.getProductById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/products/{id}", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequestDTO)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Delete product - successful")
    void testDeleteProductByIdSuccess() {
        UUID id = UUID.fromString(sampleProduct.getId());
        when(getProductPort.getProductById(id)).thenReturn(Mono.just(sampleProduct));
        when(deleteProductPort.deleteProductById(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/products/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Product deleted"));
    }

    @Test
    @DisplayName("Delete product - invalid ID")
    void testDeleteProductInvalidId() {
        webTestClient.delete()
                .uri("/api/products/invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(map -> map.get("message").equals("Invalid ID. Must be a UUID."));
    }

    @Test
    @DisplayName("Delete product - not found")
    void testDeleteProductByIdNotFound() {
        UUID randomId = UUID.randomUUID();
        when(getProductPort.getProductById(randomId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/products/{id}", randomId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Delete product - error during deletion")
    void testDeleteProductByIdError() {
        UUID randomId = UUID.randomUUID();
        when(getProductPort.getProductById(randomId)).thenReturn(Mono.just(sampleProduct));
        when(deleteProductPort.deleteProductById(randomId)).thenReturn(Mono.error(new RuntimeException("Error when deleting")));

        webTestClient.delete()
                .uri("/api/products/{id}", randomId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Product not found or error during deletion");
    }
}