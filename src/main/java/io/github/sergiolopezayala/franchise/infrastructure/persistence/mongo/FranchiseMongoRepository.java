package io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface FranchiseMongoRepository extends ReactiveMongoRepository<FranchiseDocument, String> {

    @Query(value = "{ 'name': ?0 }", exists = true, collation = FranchiseDocument.CASE_INSENSITIVE)
    Mono<Boolean> existsByName(String name);
}
