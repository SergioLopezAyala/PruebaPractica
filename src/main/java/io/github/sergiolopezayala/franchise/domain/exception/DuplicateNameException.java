package io.github.sergiolopezayala.franchise.domain.exception;

public class DuplicateNameException extends DomainException {

    public DuplicateNameException(String resource, String name) {
        super("%s with name '%s' already exists".formatted(resource, name));
    }
}
