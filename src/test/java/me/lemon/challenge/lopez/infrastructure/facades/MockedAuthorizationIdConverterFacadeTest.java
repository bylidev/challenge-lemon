package me.lemon.challenge.lopez.infrastructure.facades;

import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.infrastructure.controllers.dto.PaymentAuthorizationHttpRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MockedAuthorizationIdConverterFacadeTest {

    private final MockedAuthorizationIdConverterFacade facade = new MockedAuthorizationIdConverterFacade();

    @Test
    void shouldConvertToDomainUsingRandomIdsAndIgnoringRequestIds() {
        // Arrange
        String originalCardId = "tarjeta-123";
        String originalAccountId = "cuenta-456";
        BigDecimal amount = new BigDecimal("1000");
        String txId = "TX-789";

        PaymentAuthorizationHttpRequest request = new PaymentAuthorizationHttpRequest(
                amount,
                originalCardId,
                originalAccountId,
                txId
        );

        // Act
        AuthorizationRequest result = facade.convertToDomainId(request);

        // Assert
        assertEquals(amount, result.amount());
        assertEquals(txId, result.transactionId());
        assertNotNull(result.cardId());
        assertNotNull(result.accountId());
        assertNotEquals(originalCardId, result.cardId().toString());
        assertNotEquals(originalAccountId, result.accountId().toString());
        assertNotNull(result.transactionDate());
    }
}