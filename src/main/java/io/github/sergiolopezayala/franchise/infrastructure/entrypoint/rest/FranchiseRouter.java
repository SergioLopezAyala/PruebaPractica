package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class FranchiseRouter {

    static final String FRANCHISES = "/api/v1/franchises";
    static final String FRANCHISE = FRANCHISES + "/{franchiseId}";
    static final String BRANCHES = FRANCHISE + "/branches";
    static final String BRANCH = BRANCHES + "/{branchId}";
    static final String PRODUCTS = BRANCH + "/products";
    static final String PRODUCT = PRODUCTS + "/{productId}";

    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return route()
                .POST(FRANCHISES, contentType(APPLICATION_JSON), handler::createFranchise)
                .PATCH(FRANCHISE + "/name", contentType(APPLICATION_JSON), handler::renameFranchise)
                .POST(BRANCHES, contentType(APPLICATION_JSON), handler::addBranch)
                .PATCH(BRANCH + "/name", contentType(APPLICATION_JSON), handler::renameBranch)
                .POST(PRODUCTS, contentType(APPLICATION_JSON), handler::addProduct)
                .DELETE(PRODUCT, handler::deleteProduct)
                .PATCH(PRODUCT + "/stock", contentType(APPLICATION_JSON), handler::updateProductStock)
                .PATCH(PRODUCT + "/name", contentType(APPLICATION_JSON), handler::renameProduct)
                .GET(FRANCHISE + "/products/top-stock", handler::getTopStockProducts)
                .build();
    }
}
