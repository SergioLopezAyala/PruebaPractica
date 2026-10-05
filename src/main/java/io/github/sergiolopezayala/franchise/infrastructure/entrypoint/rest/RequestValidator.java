package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class RequestValidator {

    private final Validator validator;

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> Mono<T> body(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(() -> new RequestValidationException("Request body is required")))
                .flatMap(this::validate);
    }

    private <T> Mono<T> validate(T body) {
        var violations = validator.validate(body);
        if (violations.isEmpty()) {
            return Mono.just(body);
        }
        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return Mono.error(new RequestValidationException(message));
    }
}
