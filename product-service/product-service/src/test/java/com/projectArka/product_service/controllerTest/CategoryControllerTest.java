package com.projectArka.product_service.controllerTest;

import com.projectArka.product_service.application.dto.CategoryResponseDTO;
import com.projectArka.product_service.application.dto.CreateCategoryRequestDTO;
import com.projectArka.product_service.application.dto.UpdateCategoryRequestDTO;
import com.projectArka.product_service.application.mapper.CategoryMapper;
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
    private CategoryMapper categoryMapper;

    private Category sampleCategory;
    private CategoryResponseDTO sampleCategoryResponseDTO;
    private CreateCategoryRequestDTO createCategoryRequestDTO;
    private UpdateCategoryRequestDTO updateCategoryRequestDTO;

    @BeforeEach
    void setup() {
        createCategoryPort = mock(CreateCategoryPort.class);
        getCategoryPort = mock(GetCategoryPort.class);
        updateCategoryPort = mock(UpdateCategoryPort.class);
        deleteCategoryPort = mock(DeleteCategoryPort.class);
        categoryMapper = mock(CategoryMapper.class);

        CategoryController controller = new CategoryController(
                createCategoryPort,
                getCategoryPort,
                updateCategoryPort,
                deleteCategoryPort,
                categoryMapper
        );

        webTestClient = WebTestClient.bindToController(controller).build();

        String categoryId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        sampleCategory = Category.builder()
                .id(categoryId)
                .name("Technology")
                .description("Technological products")
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleCategoryResponseDTO = CategoryResponseDTO.builder()
                .id(categoryId)
                .name("Technology")
                .description("Technological products")
                .createdAt(now)
                .updatedAt(now)
                .build();

        createCategoryRequestDTO = new CreateCategoryRequestDTO();
        createCategoryRequestDTO.setName("Electronics");
        createCategoryRequestDTO.setDescription("Electronic devices");

        updateCategoryRequestDTO = new UpdateCategoryRequestDTO();
        updateCategoryRequestDTO.setName("Appliances");
        updateCategoryRequestDTO.setDescription("Home goods");
    }

    @Test
    void testCreateCategory() {
        when(categoryMapper.toEntity(any(CreateCategoryRequestDTO.class))).thenReturn(
                Category.builder().name("Electronics").description("Electronic devices").build()
        );
        when(createCategoryPort.createCategory(any(Category.class))).thenReturn(Mono.just(sampleCategory));

        webTestClient.post()
                .uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createCategoryRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Category created");
    }

    @Test
    void testGetCategoryById() {
        when(getCategoryPort.getCategoryById(eq(UUID.fromString(sampleCategory.getId())))).thenReturn(Mono.just(sampleCategory));
        when(categoryMapper.toDTO(sampleCategory)).thenReturn(sampleCategoryResponseDTO);

        webTestClient.get()
                .uri("/api/categories/" + sampleCategory.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Technology");
    }

    @Test
    void testGetAllCategories() {
        Category category1 = Category.builder()
                .id(UUID.randomUUID().toString())
                .name("Sports")
                .description("Sports items")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Category category2 = Category.builder()
                .id(UUID.randomUUID().toString())
                .name("Home")
                .description("Home goods")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CategoryResponseDTO dto1 = CategoryResponseDTO.builder()
                .id(category1.getId())
                .name("Sports")
                .description("Sports items")
                .createdAt(category1.getCreatedAt())
                .updatedAt(category1.getUpdatedAt())
                .build();

        CategoryResponseDTO dto2 = CategoryResponseDTO.builder()
                .id(category2.getId())
                .name("Home")
                .description("Home goods")
                .createdAt(category2.getCreatedAt())
                .updatedAt(category2.getUpdatedAt())
                .build();

        when(getCategoryPort.getAllCategories()).thenReturn(Flux.just(category1, category2));
        when(categoryMapper.toDTO(category1)).thenReturn(dto1);
        when(categoryMapper.toDTO(category2)).thenReturn(dto2);

        webTestClient.get()
                .uri("/api/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoryResponseDTO.class)
                .hasSize(2);
    }

    @Test
    void testGetCategoryByName() {
        when(getCategoryPort.getCategoryByName("Electronics")).thenReturn(Mono.just(sampleCategory));
        when(categoryMapper.toDTO(sampleCategory)).thenReturn(sampleCategoryResponseDTO);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/categories/search").queryParam("name", "Electronics").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.description").isEqualTo("Technological products");
    }

    @Test
    void testUpdateCategory() {
        when(getCategoryPort.getCategoryById(eq(UUID.fromString(sampleCategory.getId())))).thenReturn(Mono.just(sampleCategory));
        doAnswer(invocation -> {
            UpdateCategoryRequestDTO dto = invocation.getArgument(0);
            Category category = invocation.getArgument(1);
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            return null;
        }).when(categoryMapper).updateEntity(any(UpdateCategoryRequestDTO.class), any(Category.class));

        when(updateCategoryPort.updateCategory(any(Category.class))).thenReturn(Mono.just(sampleCategory));
        when(categoryMapper.toDTO(sampleCategory)).thenReturn(sampleCategoryResponseDTO);

        webTestClient.put()
                .uri("/api/categories/" + sampleCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateCategoryRequestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Technology");
    }

    @Test
    void testDeleteCategoryById() {
        UUID id = UUID.fromString(sampleCategory.getId());
        when(getCategoryPort.getCategoryById(eq(id))).thenReturn(Mono.just(sampleCategory));
        when(deleteCategoryPort.deleteCategoryById(eq(id))).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/categories/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Category deleted successfully");
    }
}

