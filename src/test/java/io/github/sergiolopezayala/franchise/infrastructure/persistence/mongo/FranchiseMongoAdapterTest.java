package io.github.sergiolopezayala.franchise.infrastructure.persistence.mongo;

import io.github.sergiolopezayala.franchise.TestcontainersConfiguration;
import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import({TestcontainersConfiguration.class, FranchiseMongoAdapter.class})
class FranchiseMongoAdapterTest {

    @Autowired
    private FranchiseMongoAdapter adapter;

    @Autowired
    private FranchiseMongoRepository repository;

    @BeforeEach
    void cleanUp() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void savesAndLoadsFranchiseWithEmbeddedBranchesAndProducts() {
        Franchise franchise = Franchise.create("Acme")
                .addBranch(Branch.create("b1", "Downtown"))
                .addProduct("b1", new Product("p1", "Coffee", 10));

        StepVerifier.create(adapter.save(franchise).flatMap(saved -> adapter.findById(saved.id())))
                .assertNext(found -> {
                    assertThat(found.id()).isNotBlank();
                    assertThat(found.version()).isZero();
                    assertThat(found.name()).isEqualTo("Acme");
                    assertThat(found.branches()).containsExactly(
                            new Branch("b1", "Downtown", List.of(new Product("p1", "Coffee", 10))));
                })
                .verifyComplete();
    }

    @Test
    void updatingIncrementsVersion() {
        StepVerifier.create(adapter.save(Franchise.create("Acme"))
                        .flatMap(saved -> adapter.save(saved.rename("Globex"))))
                .assertNext(updated -> {
                    assertThat(updated.name()).isEqualTo("Globex");
                    assertThat(updated.version()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void findByIdIsEmptyWhenMissing() {
        StepVerifier.create(adapter.findById("missing")).verifyComplete();
    }

    @Test
    void existsByNameIgnoresCase() {
        StepVerifier.create(adapter.save(Franchise.create("Acme"))
                        .then(adapter.existsByName("ACME")))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(adapter.existsByName("Globex"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void uniqueIndexRejectsDuplicateNameIgnoringCase() {
        StepVerifier.create(adapter.save(Franchise.create("Acme"))
                        .then(adapter.save(Franchise.create("acme"))))
                .expectError(DuplicateNameException.class)
                .verify();
    }

    @Test
    void staleVersionIsRejected() {
        StepVerifier.create(adapter.save(Franchise.create("Acme"))
                        .flatMap(stale -> adapter.save(stale.rename("Globex"))
                                .then(adapter.save(stale.rename("Initech")))))
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }
}
