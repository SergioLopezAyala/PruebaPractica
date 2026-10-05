package io.github.sergiolopezayala.franchise.domain.port;

import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepositoryPort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);

    Mono<Boolean> existsByName(String name);
}
