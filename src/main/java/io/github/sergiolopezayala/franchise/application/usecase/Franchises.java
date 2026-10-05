package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

final class Franchises {

    private Franchises() {
    }

    static Mono<Franchise> require(FranchiseRepositoryPort repository, String franchiseId) {
        return repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Franchise", franchiseId)));
    }

    static Mono<Franchise> saveIfNameAvailable(FranchiseRepositoryPort repository, Franchise franchise) {
        return repository.existsByName(franchise.name())
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DuplicateNameException("Franchise", franchise.name()))
                        : repository.save(franchise));
    }
}
