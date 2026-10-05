package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import io.github.sergiolopezayala.franchise.application.usecase.AddBranchUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.AddProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.CreateFranchiseUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.DeleteProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.GetTopStockProductsUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameBranchUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameFranchiseUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.RenameProductUseCase;
import io.github.sergiolopezayala.franchise.application.usecase.UpdateProductStockUseCase;
import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.model.TopStockProduct;
import io.github.sergiolopezayala.franchise.infrastructure.config.GlobalErrorHandler;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRouterTest {

    private static final String BASE = "/api/v1/franchises";

    @Mock private CreateFranchiseUseCase createFranchise;
    @Mock private RenameFranchiseUseCase renameFranchise;
    @Mock private AddBranchUseCase addBranch;
    @Mock private RenameBranchUseCase renameBranch;
    @Mock private AddProductUseCase addProduct;
    @Mock private DeleteProductUseCase deleteProduct;
    @Mock private UpdateProductStockUseCase updateProductStock;
    @Mock private RenameProductUseCase renameProduct;
    @Mock private GetTopStockProductsUseCase getTopStockProducts;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        var validator = new RequestValidator(Validation.buildDefaultValidatorFactory().getValidator());
        var handler = new FranchiseHandler(validator, createFranchise, renameFranchise, addBranch, renameBranch,
                addProduct, deleteProduct, updateProductStock, renameProduct, getTopStockProducts);
        var strategies = HandlerStrategies.empty()
                .codecs(codecs -> codecs.registerDefaults(true))
                .exceptionHandler(new GlobalErrorHandler(ServerCodecConfigurer.create()))
                .build();
        client = WebTestClient.bindToRouterFunction(new FranchiseRouter().franchiseRoutes(handler))
                .handlerStrategies(strategies)
                .build();
    }

    @Test
    void createFranchiseReturns201WithLocation() {
        when(createFranchise.execute("Acme")).thenReturn(Mono.just(new Franchise("f1", "Acme", 0L, List.of())));

        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", "Acme"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> assertThat(location).endsWith(BASE + "/f1"))
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.name").isEqualTo("Acme")
                .jsonPath("$.branches").isEmpty();
    }

    @Test
    void createFranchiseWithBlankNameReturns400() {
        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", " "))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("name: must not be blank")
                .jsonPath("$.path").isEqualTo(BASE)
                .jsonPath("$.timestamp").isNotEmpty();
        verifyNoInteractions(createFranchise);
    }

    @Test
    void createFranchiseWithoutBodyReturns400() {
        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("Request body is required");
    }

    @Test
    void createFranchiseWithMalformedJsonReturns400() {
        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON).bodyValue("{ not json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createFranchiseWithDuplicateNameReturns409() {
        when(createFranchise.execute("Acme")).thenReturn(Mono.error(new DuplicateNameException("Franchise", "Acme")));

        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", "Acme"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").isEqualTo("Franchise with name 'Acme' already exists");
    }

    @Test
    void renameFranchiseReturns200() {
        when(renameFranchise.execute("f1", "Globex"))
                .thenReturn(Mono.just(new Franchise("f1", "Globex", 1L, List.of())));

        client.patch().uri(BASE + "/f1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Globex"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Globex");
    }

    @Test
    void renameMissingFranchiseReturns404() {
        when(renameFranchise.execute("missing", "Globex"))
                .thenReturn(Mono.error(new NotFoundException("Franchise", "missing")));

        client.patch().uri(BASE + "/missing/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Globex"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Franchise missing not found")
                .jsonPath("$.path").isEqualTo(BASE + "/missing/name");
    }

    @Test
    void concurrentModificationReturns409() {
        when(renameFranchise.execute("f1", "Globex"))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("stale")));

        client.patch().uri(BASE + "/f1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Globex"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void addBranchReturns201WithLocation() {
        when(addBranch.execute("f1", "Downtown")).thenReturn(Mono.just(Branch.create("b1", "Downtown")));

        client.post().uri(BASE + "/f1/branches").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Downtown"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> assertThat(location).endsWith(BASE + "/f1/branches/b1"))
                .expectBody()
                .jsonPath("$.id").isEqualTo("b1")
                .jsonPath("$.products").isEmpty();
    }

    @Test
    void addBranchWithTooLongNameReturns400() {
        client.post().uri(BASE + "/f1/branches").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "a".repeat(101)))
                .exchange()
                .expectStatus().isBadRequest();
        verifyNoInteractions(addBranch);
    }

    @Test
    void renameBranchReturns200() {
        when(renameBranch.execute("f1", "b1", "Uptown")).thenReturn(Mono.just(Branch.create("b1", "Uptown")));

        client.patch().uri(BASE + "/f1/branches/b1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Uptown"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Uptown");
    }

    @Test
    void renameBranchToExistingNameReturns409() {
        when(renameBranch.execute("f1", "b1", "Uptown"))
                .thenReturn(Mono.error(new DuplicateNameException("Branch", "Uptown")));

        client.patch().uri(BASE + "/f1/branches/b1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Uptown"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void addProductReturns201WithLocation() {
        when(addProduct.execute("f1", "b1", "Coffee", 10)).thenReturn(Mono.just(new Product("p1", "Coffee", 10)));

        client.post().uri(BASE + "/f1/branches/b1/products").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Coffee", "stock", 10))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location ->
                        assertThat(location).endsWith(BASE + "/f1/branches/b1/products/p1"))
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1")
                .jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    void addProductWithNegativeStockReturns400() {
        client.post().uri(BASE + "/f1/branches/b1/products").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Coffee", "stock", -1))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("stock: must be greater than or equal to 0");
        verifyNoInteractions(addProduct);
    }

    @Test
    void addProductWithoutStockReturns400() {
        client.post().uri(BASE + "/f1/branches/b1/products").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Coffee"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("stock: must not be null");
    }

    @Test
    void addProductToMissingBranchReturns404() {
        when(addProduct.execute("f1", "missing", "Coffee", 10))
                .thenReturn(Mono.error(new NotFoundException("Branch", "missing")));

        client.post().uri(BASE + "/f1/branches/missing/products").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Coffee", "stock", 10))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.message").isEqualTo("Branch missing not found");
    }

    @Test
    void deleteProductReturns204() {
        when(deleteProduct.execute("f1", "b1", "p1")).thenReturn(Mono.empty());

        client.delete().uri(BASE + "/f1/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void deleteMissingProductReturns404() {
        when(deleteProduct.execute("f1", "b1", "missing"))
                .thenReturn(Mono.error(new NotFoundException("Product", "missing")));

        client.delete().uri(BASE + "/f1/branches/b1/products/missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateStockReturns200() {
        when(updateProductStock.execute("f1", "b1", "p1", 25)).thenReturn(Mono.just(new Product("p1", "Coffee", 25)));

        client.patch().uri(BASE + "/f1/branches/b1/products/p1/stock").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("stock", 25))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(25);
    }

    @Test
    void updateStockWithNegativeValueReturns400() {
        client.patch().uri(BASE + "/f1/branches/b1/products/p1/stock").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("stock", -3))
                .exchange()
                .expectStatus().isBadRequest();
        verifyNoInteractions(updateProductStock);
    }

    @Test
    void updateStockWithNonNumericValueReturns400() {
        client.patch().uri(BASE + "/f1/branches/b1/products/p1/stock").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("stock", "lots"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void renameProductReturns200() {
        when(renameProduct.execute("f1", "b1", "p1", "Espresso"))
                .thenReturn(Mono.just(new Product("p1", "Espresso", 10)));

        client.patch().uri(BASE + "/f1/branches/b1/products/p1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Espresso"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Espresso");
    }

    @Test
    void renameProductWithDomainInvalidNameReturns400() {
        when(renameProduct.execute("f1", "b1", "p1", "Espresso"))
                .thenReturn(Mono.error(new InvalidValueException("Name must not be blank")));

        client.patch().uri(BASE + "/f1/branches/b1/products/p1/name").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Espresso"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.message").isEqualTo("Name must not be blank");
    }

    @Test
    void topStockReturns200WithOneItemPerBranch() {
        when(getTopStockProducts.execute("f1")).thenReturn(Flux.just(
                new TopStockProduct("b1", "Downtown", "p2", "Tea", 30),
                new TopStockProduct("b2", "Uptown", "p3", "Milk", 7)));

        client.get().uri(BASE + "/f1/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].branchId").isEqualTo("b1")
                .jsonPath("$[0].branchName").isEqualTo("Downtown")
                .jsonPath("$[0].productId").isEqualTo("p2")
                .jsonPath("$[0].productName").isEqualTo("Tea")
                .jsonPath("$[0].stock").isEqualTo(30)
                .jsonPath("$[1].branchName").isEqualTo("Uptown");
    }

    @Test
    void topStockForMissingFranchiseReturns404() {
        when(getTopStockProducts.execute("missing"))
                .thenReturn(Flux.error(new NotFoundException("Franchise", "missing")));

        client.get().uri(BASE + "/missing/products/top-stock")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void unexpectedErrorReturns500WithoutLeakingDetails() {
        when(getTopStockProducts.execute(anyString())).thenReturn(Flux.error(new IllegalStateException("db password")));

        client.get().uri(BASE + "/f1/products/top-stock")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message").isEqualTo("Unexpected error");
    }
}
