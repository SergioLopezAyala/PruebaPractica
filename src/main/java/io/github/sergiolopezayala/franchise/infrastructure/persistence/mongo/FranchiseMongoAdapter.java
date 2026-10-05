package io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FranchiseMongoAdapter implements FranchiseRepositoryPort {

    private final FranchiseMongoRepository repository;

    public FranchiseMongoAdapter(FranchiseMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return repository.save(FranchiseDocumentMapper.toDocument(franchise))
                .map(FranchiseDocumentMapper::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new DuplicateNameException("Franchise", franchise.name()));
    }

    @Override
    public Mono<Franchise> findById(String id) {
        return repository.findById(id).map(FranchiseDocumentMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByName(name);
    }
}
