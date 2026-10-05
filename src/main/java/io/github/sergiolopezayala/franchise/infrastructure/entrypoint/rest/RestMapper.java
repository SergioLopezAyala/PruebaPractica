package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.model.TopStockProduct;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.BranchResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.FranchiseResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.ProductResponse;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.TopStockProductResponse;

final class RestMapper {

    private RestMapper() {
    }

    static FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(
                franchise.id(),
                franchise.name(),
                franchise.branches().stream().map(RestMapper::toResponse).toList());
    }

    static BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
                branch.id(),
                branch.name(),
                branch.products().stream().map(RestMapper::toResponse).toList());
    }

    static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.id(), product.name(), product.stock());
    }

    static TopStockProductResponse toResponse(TopStockProduct top) {
        return new TopStockProductResponse(
                top.branchId(), top.branchName(), top.productId(), top.productName(), top.stock());
    }
}
