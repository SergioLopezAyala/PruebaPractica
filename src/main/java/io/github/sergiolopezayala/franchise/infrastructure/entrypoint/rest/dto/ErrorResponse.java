package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto;

public record ErrorResponse(String timestamp, int status, String error, String message, String path) {
}
