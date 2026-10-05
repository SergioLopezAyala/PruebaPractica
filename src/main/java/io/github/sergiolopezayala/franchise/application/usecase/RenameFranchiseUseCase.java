package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Names;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class RenameFranchiseUseCase {

    private final FranchiseRepositoryPort repository;

    public RenameFranchiseUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Franchise> execute(String franchiseId, String newName) {
        return Franchises.require(repository, franchiseId)
                .flatMap(current -> {
                    Franchise renamed = current.rename(newName);
                    return Names.sameName(current.name(), renamed.name())
                            ? repository.save(renamed)
                            : Franchises.saveIfNameAvailable(repository, renamed);
                });
    }
}
