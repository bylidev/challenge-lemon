package me.lemon.challenge.lopez.infrastructure.controllers;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;
import org.hibernate.exception.ConstraintViolationException;

@Singleton
public class PaymentAuthorizationExceptionHandler
        implements ExceptionHandler<Throwable, HttpResponse<?>> {
    private static final String CONSTRAINT_VIOLATION_MESSAGE = "The request contains constraints violations. Please check the request data and try again.";

    @Override
    public HttpResponse<?> handle(HttpRequest request, Throwable exception) {
        return switch (exception) {
            case PaymentUnauthorizedException e ->
                    HttpResponse.status(HttpStatus.PAYMENT_REQUIRED).body(new ErrorResponse(e.getMessage()));
            case ConstraintViolationException e ->
                    HttpResponse.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(String.format(CONSTRAINT_VIOLATION_MESSAGE)));
            default -> HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        };
    }

    @Serdeable
    public record ErrorResponse(String error) {
    }
}
