package me.lemon.challenge.lopez.domain.exceptions;

public class PaymentUnauthorizedException extends RuntimeException {
    public PaymentUnauthorizedException(String msg) {
        super(msg);
    }
}
