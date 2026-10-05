package io.github.sergiolopezayala.franchise.application.usecase;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.domain.model.Branch;
import io.github.sergiolopezayala.franchise.domain.port.FranchiseRepositoryPort;
import io.github.sergiolopezayala.franchise.domain.port.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.FRANCHISE_ID;
import static io.github.sergiolopezayala.franchise.application.usecase.Fixtures.franchise;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddBranchUseCaseTest {

    @Mock
    private FranchiseRepositoryPort repository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private AddBranchUseCase useCase;

    @Test
    void addsBranchWithGeneratedId() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("b2");
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "Uptown"))
                .expectNext(new Branch("b2", "Uptown", List.of()))
                .verifyComplete();
    }

    @Test
    void failsWhenBranchNameExists() {
        when(repository.findById(FRANCHISE_ID)).thenReturn(Mono.just(franchise()));
        when(idGenerator.newId()).thenReturn("b2");

        StepVerifier.create(useCase.execute(FRANCHISE_ID, "downtown"))
                .expectError(DuplicateNameException.class)
                .verify();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenFranchiseDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", "Uptown"))
                .expectError(NotFoundException.class)
                .verify();
    }
}
