package me.lemon.challenge.lopez.infrastructure.adapters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MockedCardValidationAdapterTest {

    private MockedCardValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockedCardValidationAdapter();
    }

    @Test
    @DisplayName("Should always return true regardless of the card ID")
    void shouldAlwaysReturnTrue() {
        // Arrange
        UUID randomId = UUID.randomUUID();

        // Act
        boolean result = adapter.isValid(randomId);

        // Assert
        assertTrue(result, "El mocked adapter siempre debería validar la tarjeta como exitosa");
    }
}