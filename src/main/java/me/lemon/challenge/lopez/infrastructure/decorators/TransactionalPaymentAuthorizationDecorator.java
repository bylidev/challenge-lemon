package me.lemon.challenge.lopez.infrastructure.decorators;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;

@RequiredArgsConstructor
public class TransactionalPaymentAuthorizationDecorator implements PaymentAuthorization {
    private final PaymentAuthorization paymentAuthorization;

    @Override
    @Transactional
    public void authorize(AuthorizationRequest request) throws PaymentUnauthorizedException {
        paymentAuthorization.authorize(request);
    }
}
