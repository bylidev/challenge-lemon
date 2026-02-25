package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static me.lemon.challenge.lopez.domain.Currency.*;
import static org.junit.jupiter.api.Assertions.*;

class MockedExchangeRateAdapterTest {

    private MockedExchangeRateAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockedExchangeRateAdapter();
    }

    @ParameterizedTest(name = "Exchange rate for {0} to {1} should be {2}")
    @CsvSource({
            "ARS, USDT, 1450",
            "BTC, USDT, 67500",
            "ETH, USDT, 1950"
    })
    void shouldReturnCorrectExchangeRateForMockedPairs(Currency base, Currency quote, String expectedRate) {
        // Act
        BigDecimal result = adapter.getExchangeRate(base, quote);

        // Assert
        assertEquals(new BigDecimal(expectedRate), result);
    }

    @Test
    @DisplayName("Should throw RuntimeException when pair is not in the mock map")
    void shouldThrowExceptionForUnknownPair() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adapter.getExchangeRate(USDT, ARS);
        });
    }
}