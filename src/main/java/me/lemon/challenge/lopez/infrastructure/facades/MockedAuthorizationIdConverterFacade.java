package me.lemon.challenge.lopez.infrastructure.facades;

import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.infrastructure.controllers.dto.PaymentAuthorizationHttpRequest;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Note: Suppress warnings for this mock.
 * Used for demonstration purposes only.
 */
public class MockedAuthorizationIdConverterFacade implements DomainIdMapperFacade {
    @Override
    public AuthorizationRequest convertToDomainId(PaymentAuthorizationHttpRequest request) {
        return new AuthorizationRequest(request.amount(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                request.transactionId(),
                ZonedDateTime.now());
    }
}
