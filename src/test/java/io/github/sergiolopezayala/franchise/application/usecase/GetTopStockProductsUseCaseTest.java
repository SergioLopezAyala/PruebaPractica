package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.model.TopStockProduct;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.FRANCHISE_ID;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductsUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private GetTopStockProductsUseCase useCase;

    @Test
    void returnsTopProductPerBranchSkippingEmptyBranchesAndBreakingTies() {
        Franchise franchise = new Franchise(FRANCHISE_ID, "Acme", 1L, List.of(
                new Branch("b1", "Downtown", List.of(
                        new Product("p1", "Coffee", 10),
                        new Product("p2", "Tea", 30))),
                new Branch("b2", "Empty", List.of()),
                new Branch("b3", "Airport", List.of(
                        new Product("p3", "Water", 5),
                        new Product("p4", "Juice", 5)))));
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .expectNext(new TopStockProduct("b1", "Downtown", "p2", "Tea", 30))
                .expectNext(new TopStockProduct("b3", "Airport", "p4", "Juice", 5))
                .verifyComplete();
    }

    @Test
    void returnsEmptyWhenFranchiseHasNoProducts() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(Franchise.create("Acme")));

        StepVerifier.create(useCase.execute(FRANCHISE_ID))
                .verifyComplete();
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }
}
