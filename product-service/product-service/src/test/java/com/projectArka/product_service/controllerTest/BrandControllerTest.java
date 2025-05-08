package com.projectArka.product_service.controllerTest;

import com.projectArka.product_service.application.dto.BrandResponseDTO;
import com.projectArka.product_service.application.dto.CreateBrandRequestDTO;
import com.projectArka.product_service.application.dto.UpdateBrandRequestDTO;
import com.projectArka.product_service.application.mapper.BrandMapper;
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
import java.util.UUID;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class BrandControllerTest {

    private WebTestClient webTestClient;

    private CreateBrandPort createBrandPort;
    private GetBrandPort getBrandPort;
    private UpdateBrandPort updateBrandPort;
    private DeleteBrandPort deleteBrandPort;
    private BrandMapper brandMapper;

    private Brand sampleBrand;
    private BrandResponseDTO sampleBrandResponseDTO;
    private CreateBrandRequestDTO createBrandRequestDTO;
    private UpdateBrandRequestDTO updateBrandRequestDTO;
    private String brandId;

    @BeforeEach
    public void setUp() {
        createBrandPort = Mockito.mock(CreateBrandPort.class);
        getBrandPort = Mockito.mock(GetBrandPort.class);
        updateBrandPort = Mockito.mock(UpdateBrandPort.class);
        deleteBrandPort = Mockito.mock(DeleteBrandPort.class);
        brandMapper = Mockito.mock(BrandMapper.class);

        BrandController controller = new BrandController(
                createBrandPort,
                getBrandPort,
                updateBrandPort,
                deleteBrandPort,
                brandMapper
        );

        webTestClient = WebTestClient.bindToController(controller).build();

        brandId = UUID.randomUUID().toString();
        sampleBrand = Brand.builder()
                .id(brandId)
                .name("Nike")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleBrandResponseDTO = BrandResponseDTO.builder()
                .id(brandId)
                .name("Nike")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createBrandRequestDTO = new CreateBrandRequestDTO();
        createBrandRequestDTO.setName("Nike");

        updateBrandRequestDTO = new UpdateBrandRequestDTO();
        updateBrandRequestDTO.setName("Nike Updated");
    }

    @Test
    public void testCreateBrand() {
        when(brandMapper.toEntity(any(CreateBrandRequestDTO.class))).thenReturn(sampleBrand);
        when(createBrandPort.createBrand(any(Brand.class))).thenReturn(Mono.just(sampleBrand));

        webTestClient.post()
                .uri("/api/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(createBrandRequestDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Brand created");
    }


    @Test
    public void testGetBrandById_Found() {
        when(getBrandPort.getBrandById(any(UUID.class))).thenReturn(Mono.just(sampleBrand));
        when(brandMapper.toDTO(any(Brand.class))).thenReturn(sampleBrandResponseDTO);

        webTestClient.get()
                .uri("/api/brands/" + brandId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandById_NotFound() {
        when(getBrandPort.getBrandById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/brands/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetAllBrands() {
        when(getBrandPort.getAllBrands()).thenReturn(Flux.just(sampleBrand));
        when(brandMapper.toDTO(any(Brand.class))).thenReturn(sampleBrandResponseDTO);

        webTestClient.get()
                .uri("/api/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Nike");
    }

    @Test
    public void testGetBrandByName_Found() {
        when(getBrandPort.getBrandByName("Nike")).thenReturn(Mono.just(sampleBrand));
        when(brandMapper.toDTO(any(Brand.class))).thenReturn(sampleBrandResponseDTO);

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
        when(getBrandPort.getBrandByName("Adidas")).thenReturn(Mono.empty());

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
        when(brandMapper.toEntity(any(UpdateBrandRequestDTO.class))).thenReturn(sampleBrand);
        when(getBrandPort.getBrandById(eq(UUID.fromString(brandId)))).thenReturn(Mono.just(sampleBrand));
        when(updateBrandPort.updateBrand(any(Brand.class))).thenReturn(Mono.just(sampleBrand));
        when(brandMapper.toDTO(any(Brand.class))).thenReturn(sampleBrandResponseDTO);

        webTestClient.put()
                .uri("/api/brands/" + brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(updateBrandRequestDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Nike");
    }

    @Test
    public void testUpdateBrand_NotFound() {
        when(getBrandPort.getBrandById(eq(UUID.fromString(brandId)))).thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/api/brands/" + brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(updateBrandRequestDTO))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeleteBrandById_Success() {
        UUID brandUUID = UUID.fromString(brandId);
        when(getBrandPort.getBrandById(eq(brandUUID))).thenReturn(Mono.just(sampleBrand));
        when(deleteBrandPort.deleteBrandById(eq(brandUUID))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/brands/" + brandId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Brand deleted");
    }

    @Test
    public void testDeleteBrandById_Error() {
        UUID brandUUID = UUID.fromString(brandId);
        when(getBrandPort.getBrandById(eq(brandUUID))).thenReturn(Mono.just(sampleBrand));
        when(deleteBrandPort.deleteBrandById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Error")));

        webTestClient.delete()
                .uri("/api/brands/" + brandId)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}