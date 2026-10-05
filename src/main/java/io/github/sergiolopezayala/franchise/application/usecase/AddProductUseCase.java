package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import io.github.sergiolopezayala.franchise.domain.port.IdGenerator;
import reactor.core.publisher.Mono;

public class AddProductUseCase {

    private final FranchiseRepositoryPort repository;
    private final IdGenerator idGenerator;

    public AddProductUseCase(FranchiseRepositoryPort repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public Mono<Product> execute(String franchiseId, String branchId, String name, int stock) {
        return Franchises.require(repository, franchiseId)
                .flatMap(franchise -> {
                    Product product = new Product(idGenerator.newId(), name, stock);
                    return repository.save(franchise.addProduct(branchId, product))
                            .map(saved -> saved.findBranch(branchId).findProduct(product.id()));
                });
    }
}
