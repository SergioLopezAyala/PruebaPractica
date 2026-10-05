package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import io.github.sergiolopezayala.franchise.domain.port.IdGenerator;
import reactor.core.publisher.Mono;

public class AddBranchUseCase {

    private final FranchiseRepositoryPort repository;
    private final IdGenerator idGenerator;

    public AddBranchUseCase(FranchiseRepositoryPort repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public Mono<Branch> execute(String franchiseId, String name) {
        return Franchises.require(repository, franchiseId)
                .flatMap(franchise -> {
                    Branch branch = Branch.create(idGenerator.newId(), name);
                    return repository.save(franchise.addBranch(branch))
                            .map(saved -> saved.findBranch(branch.id()));
                });
    }
}
