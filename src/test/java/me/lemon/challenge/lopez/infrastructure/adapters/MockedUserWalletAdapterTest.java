package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockedUserWalletAdapterTest {

    private MockedUserWalletAdapter adapter;
    private UUID accountId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        adapter = new MockedUserWalletAdapter();
        accountId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Test
    void shouldReturnDefaultWalletWithInitialBalances() {
        // Act
        Optional<Wallet> walletOpt = adapter.getDefaultWallet(accountId);

        // Assert
        assertTrue(walletOpt.isPresent());
        Wallet wallet = walletOpt.get();
        assertEquals(Currency.BTC, wallet.currency());
        assertEquals(new BigDecimal("100"), wallet.available());
        assertEquals(BigDecimal.ZERO, wallet.held());
    }

    @Test
    void shouldIncreaseHeldBalanceWhenApplyingHold() {
        // Arrange
        Wallet wallet = adapter.getDefaultWallet(accountId).get();
        BigDecimal amount = new BigDecimal("10.5");

        // Act
        adapter.applyHold(idempotencyKey, wallet, amount);

        // Assert
        Wallet updatedWallet = adapter.getDefaultWallet(accountId).get();
        assertEquals(0, amount.compareTo(updatedWallet.held()), "El saldo retenido debería ser 10.5");
    }

    @Test
    void shouldDecreaseHeldBalanceWhenReleasingHold() {
        // Arrange
        Wallet wallet = adapter.getDefaultWallet(accountId).get();
        BigDecimal holdAmount = new BigDecimal("50");
        BigDecimal releaseAmount = new BigDecimal("20");

        adapter.applyHold(idempotencyKey, wallet, holdAmount);

        // Act
        adapter.releaseHold(idempotencyKey, wallet, releaseAmount);

        // Assert
        Wallet updatedWallet = adapter.getDefaultWallet(accountId).get();
        assertEquals(0, new BigDecimal("30").compareTo(updatedWallet.held()), "El saldo retenido debería haber bajado a 30");
    }
}