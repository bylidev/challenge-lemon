package me.lemon.challenge.lopez.infrastructure.facades;

import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.infrastructure.controllers.dto.PaymentAuthorizationHttpRequest;

public interface DomainIdMapperFacade {
    AuthorizationRequest convertToDomainId(PaymentAuthorizationHttpRequest request);
}
