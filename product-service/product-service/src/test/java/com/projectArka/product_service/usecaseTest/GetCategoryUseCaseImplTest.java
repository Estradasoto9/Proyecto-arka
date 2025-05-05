package com.projectArka.product_service.usecaseTest;

import com.projectArka.product_service.application.usecase.GetCategoryUseCaseImpl;
import com.projectArka.product_service.domain.model.Category;
import com.projectArka.product_service.domain.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GetCategoryUseCaseImplTest {

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    @InjectMocks
    private GetCategoryUseCaseImpl getCategoryUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCategoryById_success() {
        UUID categoryId = UUID.randomUUID();
        Category expectedCategory = Category.builder()
                .id(categoryId.toString())
                .name("Electrónicos")
                .description("Categoría de productos electrónicos")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(categoryRepositoryPort.findById(categoryId.toString())).thenReturn(Mono.just(expectedCategory));

        Mono<Category> result = getCategoryUseCase.getCategoryById(categoryId);

        StepVerifier.create(result)
                .expectNext(expectedCategory)
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).findById(categoryId.toString());
    }

    @Test
    void testGetCategoryById_notFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepositoryPort.findById(categoryId.toString())).thenReturn(Mono.empty());

        Mono<Category> result = getCategoryUseCase.getCategoryById(categoryId);

        StepVerifier.create(result)
                .verifyComplete();
        verify(categoryRepositoryPort, times(1)).findById(categoryId.toString());
    }

    @Test
    void testGetAllCategories_success() {
        Category category1 = Category.builder().id("1").name("Electrónicos").description("Desc 1").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        Category category2 = Category.builder().id("2").name("Ropa").description("Desc 2").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(categoryRepositoryPort.findAll()).thenReturn(Flux.just(category1, category2));

        Flux<Category> result = getCategoryUseCase.getAllCategories();

        StepVerifier.create(result)
                .expectNext(category1, category2)
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).findAll();
    }

    @Test
    void testGetAllCategories_empty() {
        when(categoryRepositoryPort.findAll()).thenReturn(Flux.empty());

        Flux<Category> result = getCategoryUseCase.getAllCategories();

        StepVerifier.create(result)
                .verifyComplete();
        verify(categoryRepositoryPort, times(1)).findAll();
    }

    @Test
    void testGetCategoryByName_success() {
        String categoryName = "Electrónicos";
        Category expectedCategory = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(categoryName)
                .description("Descripción de la categoría")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(categoryRepositoryPort.findByName(categoryName)).thenReturn(Mono.just(expectedCategory));

        Mono<Category> result = getCategoryUseCase.getCategoryByName(categoryName);

        StepVerifier.create(result)
                .expectNext(expectedCategory)
                .verifyComplete();

        verify(categoryRepositoryPort, times(1)).findByName(categoryName);
    }

    @Test
    void testGetCategoryByName_notFound() {
        String categoryName = "NonExistentCategory";
        when(categoryRepositoryPort.findByName(categoryName)).thenReturn(Mono.empty());

        Mono<Category> result = getCategoryUseCase.getCategoryByName(categoryName);

        StepVerifier.create(result)
                .verifyComplete();
        verify(categoryRepositoryPort, times(1)).findByName(categoryName);
    }
}