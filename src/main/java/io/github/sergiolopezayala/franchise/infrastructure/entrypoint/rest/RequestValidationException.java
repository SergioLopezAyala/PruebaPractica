package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest;

public class RequestValidationException extends RuntimeException {

    public RequestValidationException(String message) {
        super(message);
    }
}
