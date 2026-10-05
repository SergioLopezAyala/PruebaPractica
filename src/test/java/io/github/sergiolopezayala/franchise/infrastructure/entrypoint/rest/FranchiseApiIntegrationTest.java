package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import io.github.sergiolopezayala.franchise.TestcontainersConfiguration;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.BranchResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.FranchiseResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
class FranchiseApiIntegrationTest {

    private static final String BASE = "/api/v1/franchises";

    @Autowired
    private WebTestClient client;

    @Test
    void fullFranchiseLifecycle() {
        String franchiseName = "Acme " + UUID.randomUUID();
        FranchiseResponse franchise = post(BASE, Map.of("name", franchiseName), FranchiseResponse.class);
        String franchiseUri = BASE + "/" + franchise.id();

        BranchResponse downtown = post(franchiseUri + "/branches", Map.of("name", "Downtown"), BranchResponse.class);
        BranchResponse uptown = post(franchiseUri + "/branches", Map.of("name", "Uptown"), BranchResponse.class);
        post(franchiseUri + "/branches", Map.of("name", "Empty"), BranchResponse.class);

        String downtownProducts = franchiseUri + "/branches/" + downtown.id() + "/products";
        String uptownProducts = franchiseUri + "/branches/" + uptown.id() + "/products";
        ProductResponse coffee = post(downtownProducts, Map.of("name", "Coffee", "stock", 10), ProductResponse.class);
        ProductResponse tea = post(downtownProducts, Map.of("name", "Tea", "stock", 5), ProductResponse.class);
        ProductResponse milk = post(uptownProducts, Map.of("name", "Milk", "stock", 7), ProductResponse.class);
        post(uptownProducts, Map.of("name", "Juice", "stock", 7), ProductResponse.class);

        client.patch().uri(downtownProducts + "/" + tea.id() + "/stock")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("stock", 40))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(40);

        client.patch().uri(downtownProducts + "/" + tea.id() + "/name")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", "Green tea"))
                .exchange()
                .expectStatus().isOk();

        client.delete().uri(downtownProducts + "/" + coffee.id())
                .exchange()
                .expectStatus().isNoContent();

        client.patch().uri(franchiseUri + "/branches/" + uptown.id() + "/name")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", "Airport"))
                .exchange()
                .expectStatus().isOk();

        client.patch().uri(franchiseUri + "/name")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of("name", franchiseName + " renamed"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo(franchiseName + " renamed");

        client.get().uri(franchiseUri + "/products/top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].branchId").isEqualTo(downtown.id())
                .jsonPath("$[0].productId").isEqualTo(tea.id())
                .jsonPath("$[0].productName").isEqualTo("Green tea")
                .jsonPath("$[0].stock").isEqualTo(40)
                .jsonPath("$[1].branchName").isEqualTo("Airport")
                .jsonPath("$[1].productName").isEqualTo("Juice");

        assertThat(milk.id()).isNotBlank();
    }

    @Test
    void duplicateFranchiseNameReturns409IgnoringCase() {
        String name = "Globex " + UUID.randomUUID();
        post(BASE, Map.of("name", name), FranchiseResponse.class);

        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name.toUpperCase()))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.error").isEqualTo("Conflict");
    }

    @Test
    void missingFranchiseReturns404ErrorBody() {
        client.get().uri(BASE + "/does-not-exist/products/top-stock")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Franchise does-not-exist not found")
                .jsonPath("$.path").isEqualTo(BASE + "/does-not-exist/products/top-stock");
    }

    @Test
    void malformedJsonReturns400ErrorBody() {
        client.post().uri(BASE).contentType(MediaType.APPLICATION_JSON).bodyValue("{ not json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void unknownRouteReturns404ErrorBody() {
        client.get().uri("/api/v1/unknown")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void openApiDocumentsEveryRoute() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths['/api/v1/franchises'].post.operationId").isEqualTo("createFranchise")
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/name'].patch").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches'].post").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches/{branchId}/name'].patch").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches/{branchId}/products'].post").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}'].delete")
                .exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock']"
                        + ".patch").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name']"
                        + ".patch").exists()
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/products/top-stock'].get").exists();
    }

    private <T> T post(String uri, Map<String, ?> body, Class<T> type) {
        return client.post().uri(uri).contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .expectBody(type)
                .returnResult()
                .getResponseBody();
    }
}
