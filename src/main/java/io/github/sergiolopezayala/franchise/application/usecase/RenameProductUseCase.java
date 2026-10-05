package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class RenameProductUseCase {

    private final FranchiseRepositoryPort repository;

    public RenameProductUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String productId, String newName) {
        return Franchises.require(repository, franchiseId)
                .map(franchise -> franchise.renameProduct(branchId, productId, newName))
                .flatMap(repository::save)
                .map(saved -> saved.findBranch(branchId).findProduct(productId));
    }
}
