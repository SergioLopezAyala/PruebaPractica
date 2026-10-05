package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.TopStockProduct;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Flux;

public class GetTopStockProductsUseCase {

    private final FranchiseRepositoryPort repository;

    public GetTopStockProductsUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Flux<TopStockProduct> execute(String franchiseId) {
        return Franchises.require(repository, franchiseId)
                .flatMapIterable(Franchise::topStockPerBranch);
    }
}
