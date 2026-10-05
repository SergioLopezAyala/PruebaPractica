package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.model.Franchise;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @InjectMocks
    private CreateFranchiseUseCase useCase;

    @Test
    void createsFranchiseWithTrimmedName() {
        Franchise saved = new Franchise("f1", "Acme", 0L, List.of());
        when(repository.existsByName("Acme")).thenReturn(Mono.just(false));
        when(repository.save(Franchise.create("Acme"))).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("  Acme "))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void failsWhenNameAlreadyExists() {
        when(repository.existsByName("Acme")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute("Acme"))
                .expectError(DuplicateNameException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenNameIsBlank() {
        StepVerifier.create(useCase.execute(" "))
                .expectError(InvalidValueException.class)
                .verify();
        verifyNoInteractions(repository);
    }
}
