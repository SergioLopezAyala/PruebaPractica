package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.BRANCH_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.FRANCHISE_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.PRODUCT_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.franchise;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductStockUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private UpdateProductStockUseCase useCase;

    @Test
    void updatesStock() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 50))
                .expectNext(new Product(PRODUCT_ID, "Coffee", 50))
                .verifyComplete();
    }

    @Test
    void failsWhenStockIsNegative() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, -1))
                .expectError(InvalidValueException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenProductDoesNotExist() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "missing", 5))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", BRANCH_ID, PRODUCT_ID, 5))
                .expectError(NotFoundException.class)
                .verify();
    }
}
