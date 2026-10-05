package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class UpdateProductStockUseCase {

    private final FranchiseRepositoryPort repository;

    public UpdateProductStockUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, int stock) {
        return Franchises.require(repository, franchiseId)
                .map(franchise -> franchise.updateProductStock(branchId, productId, stock))
                .flatMap(repository::save)
                .map(saved -> saved.findBranch(branchId).findProduct(productId));
    }
}
