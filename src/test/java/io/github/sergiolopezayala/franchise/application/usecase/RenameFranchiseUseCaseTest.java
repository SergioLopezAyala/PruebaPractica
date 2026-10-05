package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.FRANCHISE_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.franchise;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenameFranchiseUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private RenameFranchiseUseCase useCase;

    @Test
    void renamesFranchise() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(repository.existsByName("Globex")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Globex"))
                .expectNextMatches(renamed -> renamed.name().equals("Globex"))
                .verifyComplete();
    }

    @Test
    void changingOnlyTheCaseSkipsTheUniquenessCheck() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "ACME"))
                .expectNextMatches(renamed -> renamed.name().equals("ACME"))
                .verifyComplete();
        verify(repository, never()).existsByName(anyString());
    }

    @Test
    void failsWhenNameBelongsToAnotherFranchise() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(repository.existsByName("Globex")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Globex"))
                .expectError(DuplicateNameException.class)
                .verify();
        verify(repository, never()).save(any(Franchise.class));
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", "Globex"))
                .expectErrorMatches(error -> error instanceof NotFoundException
                        && error.getMessage().equals("Franchise missing not found"))
                .verify();
    }

    @Test
    void failsWhenNameIsInvalid() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, ""))
                .expectError(InvalidValueException.class)
                .verify();
        verify(repository, never()).save(any());
    }
}
