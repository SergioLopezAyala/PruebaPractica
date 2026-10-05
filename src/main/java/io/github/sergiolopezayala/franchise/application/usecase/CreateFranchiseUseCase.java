package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class CreateFranchiseUseCase {

    private final FranchiseRepositoryPort repository;

    public CreateFranchiseUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Franchise> execute(String name) {
        return Mono.fromCallable(() -> Franchise.create(name))
                .flatMap(franchise -> Franchises.saveIfNameAvailable(repository, franchise));
    }
}
