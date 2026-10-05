package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class DeleteProductUseCase {

    private final FranchiseRepositoryPort repository;

    public DeleteProductUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Void> execute(String franchiseId, String branchId, String productId) {
        return Franchises.require(repository, franchiseId)
                .map(franchise -> franchise.removeProduct(branchId, productId))
                .flatMap(repository::save)
                .then();
    }
}
