package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Product;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import io.github.sergiolopezayala.franchise.domain.port.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.BRANCH_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.FRANCHISE_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.franchise;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private AddProductUseCase useCase;

    @Test
    void addsProductWithGeneratedId() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("p3");
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Milk", 7))
                .expectNext(new Product("p3", "Milk", 7))
                .verifyComplete();
    }

    @Test
    void failsWhenProductNameExistsInBranch() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("p3");

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "COFFEE", 1))
                .expectError(DuplicateNameException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenStockIsNegative() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("p3");

        StepVerifier.create(useCase.execute(FRANCHISE_ID, BRANCH_ID, "Milk", -1))
                .expectError(InvalidValueException.class)
                .verify();
    }

    @Test
    void failsWhenBranchDoesNotExist() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("p3");

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "missing", "Milk", 7))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", BRANCH_ID, "Milk", 7))
                .expectError(NotFoundException.class)
                .verify();
    }
}
