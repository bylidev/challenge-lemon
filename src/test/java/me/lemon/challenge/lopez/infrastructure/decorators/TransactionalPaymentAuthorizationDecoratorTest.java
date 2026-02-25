package me.lemon.challenge.lopez.infrastructure.decorators;

import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class TransactionalPaymentAuthorizationDecoratorTest {
    private PaymentAuthorization paymentAuthorization;
    private TransactionalPaymentAuthorizationDecorator decorator;

    @BeforeEach
    void setUp() {
        paymentAuthorization = mock(PaymentAuthorization.class);
        decorator = new TransactionalPaymentAuthorizationDecorator(paymentAuthorization);
    }

    @Test
    void shouldDelegateAuthorizationToWrappedInstance() throws PaymentUnauthorizedException {
        var request = mock(AuthorizationRequest.class);
        decorator.authorize(request);
        verify(paymentAuthorization, times(1)).authorize(request);
    }
}

