package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.BranchResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.CreateProductRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.ErrorResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.FranchiseResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.NameRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.ProductResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.StockRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.TopStockProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @RouterOperations({
        @RouterOperation(path = FRANCHISES, method = RequestMethod.POST,
            beanClass = FranchiseHandler.class, beanMethod = "createFranchise",
            operation = @Operation(operationId = "createFranchise", summary = "Create a franchise",
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                        content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = FRANCHISE + "/name", method = RequestMethod.PATCH,
            beanClass = FranchiseHandler.class, beanMethod = "renameFranchise",
            operation = @Operation(operationId = "renameFranchise", summary = "Rename a franchise",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = FranchiseResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = BRANCHES, method = RequestMethod.POST,
            beanClass = FranchiseHandler.class, beanMethod = "addBranch",
            operation = @Operation(operationId = "addBranch", summary = "Add a branch to a franchise",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                        content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = BRANCH + "/name", method = RequestMethod.PATCH,
            beanClass = FranchiseHandler.class, beanMethod = "renameBranch",
            operation = @Operation(operationId = "renameBranch", summary = "Rename a branch",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = BranchResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = PRODUCTS, method = RequestMethod.POST,
            beanClass = FranchiseHandler.class, beanMethod = "addProduct",
            operation = @Operation(operationId = "addProduct", summary = "Add a product to a branch",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = CreateProductRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                        content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = PRODUCT, method = RequestMethod.DELETE,
            beanClass = FranchiseHandler.class, beanMethod = "deleteProduct",
            operation = @Operation(operationId = "deleteProduct", summary = "Delete a product from a branch",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)},
                responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = PRODUCT + "/stock", method = RequestMethod.PATCH,
            beanClass = FranchiseHandler.class, beanMethod = "updateProductStock",
            operation = @Operation(operationId = "updateProductStock", summary = "Update a product's stock",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = StockRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = PRODUCT + "/name", method = RequestMethod.PATCH,
            beanClass = FranchiseHandler.class, beanMethod = "renameProduct",
            operation = @Operation(operationId = "renameProduct", summary = "Rename a product",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "branchId", required = true),
                    @Parameter(in = ParameterIn.PATH, name = "productId", required = true)},
                requestBody = @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = NameRequest.class))),
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name in use or concurrent update",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})),
        @RouterOperation(path = FRANCHISE + "/products/top-stock", method = RequestMethod.GET,
            beanClass = FranchiseHandler.class, beanMethod = "getTopStockProducts",
            operation = @Operation(operationId = "getTopStockProducts",
                summary = "Product with the most stock in each branch",
                parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "franchiseId", required = true)},
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = TopStockProductResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "Not found",
                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))}))
    })
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
