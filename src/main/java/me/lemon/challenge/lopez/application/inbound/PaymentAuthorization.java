package me.lemon.challenge.lopez.application.inbound;

import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;

public interface PaymentAuthorization {
    void authorize(AuthorizationRequest request) throws PaymentUnauthorizedException;
}
