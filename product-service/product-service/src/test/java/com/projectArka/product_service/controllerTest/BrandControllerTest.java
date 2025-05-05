package com.projectArka.product_service.controllerTest;

import com.projectArka.product_service.domain.model.Brand;
import com.projectArka.product_service.domain.port.in.*;
import com.projectArka.product_service.infrastructure.adapter.in.webflux.BrandController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class BrandControllerTest {

    private WebTestClient webTestClient;

    private CreateBrandPort createBrandPort;
    private GetBrandPort getBrandPort;
    private UpdateBrandPort updateBrandPort;
    private DeleteBrandPort deleteBrandPort;

    private Brand sampleBrand;

    @BeforeEach
    public void setUp() {
        createBrandPort = Mockito.mock(CreateBrandPort.class);
        getBrandPort = Mockito.mock(GetBrandPort.class);
        updateBrandPort = Mockito.mock(UpdateBrandPort.class);
        deleteBrandPort = Mockito.mock(DeleteBrandPort.class);

        BrandController controller = new BrandController(
                createBrandPort,
                getBrandPort,
                updateBrandPort,
                deleteBrandPort
        );

        webTestClient = WebTestClient.bindToController(controller).build();

        sampleBrand = Brand.builder()
                .id(UUID.randomUUID().toString())
                .name("Nike")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    public void testCreateBrand() {
        Mockito.when(createBrandPort.createBrand(any())).thenReturn(Mono.just(sampleBrand));

        webTestClient.post()
                .uri("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(sampleBrand))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandById_Found() {
        Mockito.when(getBrandPort.getBrandById(any())).thenReturn(Mono.just(sampleBrand));

        webTestClient.get()
                .uri("/api/brands/" + sampleBrand.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandById_NotFound() {
        Mockito.when(getBrandPort.getBrandById(any())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/brands/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetAllBrands() {
        Mockito.when(getBrandPort.getAllBrands()).thenReturn(Flux.just(sampleBrand));

        webTestClient.get()
                .uri("/api/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandByName_Found() {
        Mockito.when(getBrandPort.getBrandByName("Nike")).thenReturn(Mono.just(sampleBrand));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/brands/search")
                        .queryParam("name", "Nike")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandByName_NotFound() {
        Mockito.when(getBrandPort.getBrandByName("Adidas")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/brands/search")
                        .queryParam("name", "Adidas")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testUpdateBrand_Success() {
        Mockito.when(updateBrandPort.updateBrand(any())).thenReturn(Mono.just(sampleBrand));

        webTestClient.put()
                .uri("/api/brands/" + sampleBrand.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(sampleBrand))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testUpdateBrand_NotFound() {
        Mockito.when(updateBrandPort.updateBrand(any())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/brands/" + sampleBrand.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(sampleBrand))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeleteBrandById_Success() {
        UUID brandUUID = UUID.fromString(sampleBrand.getId()); // sampleBrand ya tiene un UUID en string

        Mockito.when(getBrandPort.getBrandById(eq(brandUUID)))
                .thenReturn(Mono.just(sampleBrand));
        Mockito.when(deleteBrandPort.deleteBrandById(eq(brandUUID)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/brands/" + brandUUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Marca eliminada");
    }

    @Test
    public void testDeleteBrandById_Error() {
        Mockito.when(deleteBrandPort.deleteBrandById(any())).thenReturn(Mono.error(new RuntimeException("Error")));

        webTestClient.delete()
                .uri("/api/brands/" + sampleBrand.getId())
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
