package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import reactor.core.publisher.Mono;

public class RenameBranchUseCase {

    private final FranchiseRepositoryPort repository;

    public RenameBranchUseCase(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Branch> execute(String franchiseId, String branchId, String newName) {
        return Franchises.require(repository, franchiseId)
                .map(franchise -> franchise.renameBranch(branchId, newName))
                .flatMap(repository::save)
                .map(saved -> saved.findBranch(branchId));
    }
}
