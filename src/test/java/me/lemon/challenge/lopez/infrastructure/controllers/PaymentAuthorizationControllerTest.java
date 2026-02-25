package me.lemon.challenge.lopez.infrastructure.controllers;

import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.infrastructure.controllers.dto.PaymentAuthorizationHttpRequest;
import me.lemon.challenge.lopez.infrastructure.facades.DomainIdMapperFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorizationControllerTest {

    @Mock
    private PaymentAuthorization paymentAuthorization;

    @Mock
    private DomainIdMapperFacade domainIdMapperFacade;

    @InjectMocks
    private PaymentAuthorizationController controller;

    private PaymentAuthorizationHttpRequest request;

    @BeforeEach
    void setUp() {
        request = new PaymentAuthorizationHttpRequest(
            new BigDecimal("100.00"),
            "card123",
            "account456",
            "transaction789"
        );
    }

    @Test
    void shouldAuthorizePayment() {
        // Arrange
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(
            new BigDecimal("100.00"),
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            "transaction789",
            ZonedDateTime.now()
        );
        when(domainIdMapperFacade.convertToDomainId(request)).thenReturn(authorizationRequest);

        // Act
        controller.authorize(request);

        // Assert
        verify(paymentAuthorization).authorize(authorizationRequest);
    }
}
