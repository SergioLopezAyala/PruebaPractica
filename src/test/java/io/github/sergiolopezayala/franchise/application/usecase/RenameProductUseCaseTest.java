package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
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
class RenameProductUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private RenameProductUseCase useCase;

    @Test
    void renamesProduct() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Espresso"))
                .expectNext(new Product(PRODUCT_ID, "Espresso", 10))
                .verifyComplete();
    }

    @Test
    void failsWhenNameBelongsToAnotherProduct() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Tea"))
                .expectError(DuplicateNameException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenProductDoesNotExist() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "missing", "Espresso"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", BRANCH_ID, PRODUCT_ID, "Espresso"))
                .expectError(NotFoundException.class)
                .verify();
    }
}
