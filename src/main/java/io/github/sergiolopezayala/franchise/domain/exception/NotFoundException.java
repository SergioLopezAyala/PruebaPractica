package io.github.sergiolopezayala.franchise.domain.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(String resource, String id) {
        super("%s %s not found".formatted(resource, id));
    }
}
