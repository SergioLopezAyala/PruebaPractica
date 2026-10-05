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
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.CreateProductRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.NameRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.StockRequest;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.TopStockProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private static final String FRANCHISE_ID = "franchiseId";
    private static final String BRANCH_ID = "branchId";
    private static final String PRODUCT_ID = "productId";

    private final RequestValidator validator;
    private final CreateFranchiseUseCase createFranchise;
    private final RenameFranchiseUseCase renameFranchise;
    private final AddBranchUseCase addBranch;
    private final RenameBranchUseCase renameBranch;
    private final AddProductUseCase addProduct;
    private final DeleteProductUseCase deleteProduct;
    private final UpdateProductStockUseCase updateProductStock;
    private final RenameProductUseCase renameProduct;
    private final GetTopStockProductsUseCase getTopStockProducts;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> createFranchise.execute(body.name()))
                .flatMap(franchise -> ServerResponse.created(childUri(request, franchise.id()))
                        .bodyValue(RestMapper.toResponse(franchise)));
    }

    public Mono<ServerResponse> renameFranchise(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> renameFranchise.execute(request.pathVariable(FRANCHISE_ID), body.name()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(RestMapper.toResponse(franchise)));
    }

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> addBranch.execute(request.pathVariable(FRANCHISE_ID), body.name()))
                .flatMap(branch -> ServerResponse.created(childUri(request, branch.id()))
                        .bodyValue(RestMapper.toResponse(branch)));
    }

    public Mono<ServerResponse> renameBranch(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> renameBranch.execute(
                        request.pathVariable(FRANCHISE_ID), request.pathVariable(BRANCH_ID), body.name()))
                .flatMap(branch -> ServerResponse.ok().bodyValue(RestMapper.toResponse(branch)));
    }

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        return validator.body(request, CreateProductRequest.class)
                .flatMap(body -> addProduct.execute(
                        request.pathVariable(FRANCHISE_ID), request.pathVariable(BRANCH_ID),
                        body.name(), body.stock()))
                .flatMap(product -> ServerResponse.created(childUri(request, product.id()))
                        .bodyValue(RestMapper.toResponse(product)));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        return deleteProduct.execute(
                        request.pathVariable(FRANCHISE_ID),
                        request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        return validator.body(request, StockRequest.class)
                .flatMap(body -> updateProductStock.execute(
                        request.pathVariable(FRANCHISE_ID), request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID), body.stock()))
                .flatMap(product -> ServerResponse.ok().bodyValue(RestMapper.toResponse(product)));
    }

    public Mono<ServerResponse> renameProduct(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> renameProduct.execute(
                        request.pathVariable(FRANCHISE_ID), request.pathVariable(BRANCH_ID),
                        request.pathVariable(PRODUCT_ID), body.name()))
                .flatMap(product -> ServerResponse.ok().bodyValue(RestMapper.toResponse(product)));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return getTopStockProducts.execute(request.pathVariable(FRANCHISE_ID))
                .map(RestMapper::toResponse)
                .collectList()
                .flatMap(products -> ServerResponse.ok().bodyValue(products));
    }

    private static URI childUri(ServerRequest request, String id) {
        return request.uriBuilder().path("/{id}").build(id);
    }
}
