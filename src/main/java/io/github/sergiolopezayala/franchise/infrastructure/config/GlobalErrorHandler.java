package io.github.sergiolopezayala.franchise.infrastructure.config;

import io.github.sergiolopezayala.franchise.domain.exception.DuplicateNameException;
import io.github.sergiolopezayala.franchise.domain.exception.InvalidValueException;
import io.github.sergiolopezayala.franchise.domain.exception.NotFoundException;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.RequestValidationException;
import io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {

    private static final String CONCURRENT_UPDATE = "The resource was modified by another request, please retry";
    private static final String UNEXPECTED_ERROR = "Unexpected error";

    private final ServerResponse.Context responseContext;

    public GlobalErrorHandler(ServerCodecConfigurer codecs) {
        this.responseContext = new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return codecs.getWriters();
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return List.of();
            }
        };
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(error);
        }
        HttpStatusCode status = statusOf(error);
        if (status.is5xxServerError()) {
            log.error("Unhandled error on {}", exchange.getRequest().getPath(), error);
        }
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                reasonPhrase(status),
                messageOf(error, status),
                exchange.getRequest().getPath().value());
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .flatMap(response -> response.writeTo(exchange, responseContext));
    }

    private static HttpStatusCode statusOf(Throwable error) {
        if (error instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (error instanceof DuplicateNameException || error instanceof OptimisticLockingFailureException) {
            return HttpStatus.CONFLICT;
        }
        if (error instanceof InvalidValueException || error instanceof RequestValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (error instanceof ResponseStatusException statusError) {
            return statusError.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static String messageOf(Throwable error, HttpStatusCode status) {
        if (error instanceof OptimisticLockingFailureException) {
            return CONCURRENT_UPDATE;
        }
        if (error instanceof ResponseStatusException statusError) {
            return statusError.getReason() != null ? statusError.getReason() : reasonPhrase(status);
        }
        return status.is5xxServerError() ? UNEXPECTED_ERROR : error.getMessage();
    }

    private static String reasonPhrase(HttpStatusCode status) {
        HttpStatus known = HttpStatus.resolve(status.value());
        return known != null ? known.getReasonPhrase() : String.valueOf(status.value());
    }
}
