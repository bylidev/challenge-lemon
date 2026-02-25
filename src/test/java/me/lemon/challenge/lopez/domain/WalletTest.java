package me.lemon.challenge.lopez.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
class WalletTest {

        @Test
        void shouldCreateWalletWithValidData() {
            // Arrange
            String id = "wallet-123";
            Currency currency = Currency.USD;
            BigDecimal available = new BigDecimal("100.00");
            BigDecimal held = new BigDecimal("20.00");

            // Act
            Wallet wallet = Wallet.of(id, currency, available, held);

            // Assert
            assertEquals(id, wallet.id());
            assertEquals(currency, wallet.currency());
            assertEquals(0, available.compareTo(wallet.available()));
            assertEquals(0, held.compareTo(wallet.held()));
        }

        @Test
        void shouldThrowExceptionWhenHeldIsGreaterThanAvailable() {
            // Arrange
            BigDecimal available = new BigDecimal("50.00");
            BigDecimal held = new BigDecimal("60.00");

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                Wallet.of("id", Currency.USD, available, held);
            });

            assertEquals("held > available", exception.getMessage());
        }

        @Test
        void shouldReturnCorrectTotal() {
            // Arrange
            BigDecimal available = new BigDecimal("100.50");
            BigDecimal held = new BigDecimal("50.25");
            Wallet wallet = Wallet.of("id", Currency.USD, available, held);
            BigDecimal expectedTotal = new BigDecimal("150.75");

            // Act
            BigDecimal result = wallet.total();

            // Assert
            assertEquals(0, expectedTotal.compareTo(result), "El total calculado no es el esperado");
        }

        @Test
        @DisplayName("Debe permitir que held sea exactamente igual a available")
        void shouldAllowEqualityBetweenHeldAndAvailable() {
            // Arrange
            BigDecimal value = new BigDecimal("100.00");

            // Act & Assert
            assertDoesNotThrow(() -> {
                Wallet.of("id", Currency.USD, value, value);
            });
        }
}