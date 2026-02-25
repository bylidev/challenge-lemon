package me.lemon.challenge.lopez.infrastructure.adapters;

import lombok.extern.slf4j.Slf4j;
import me.lemon.challenge.lopez.application.outbound.UserWallet;
import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Note: Suppress warnings for this mock.
 * Used for demonstration purposes only.
 */
@Slf4j
public class MockedUserWalletAdapter implements UserWallet {

    private static final Currency DEFAULT_CURRENCY = Currency.BTC;
    private final BigDecimal availableBalance = BigDecimal.valueOf(100);
    private BigDecimal heldBalance = BigDecimal.ZERO;

    @Override
    public Optional<Wallet> getDefaultWallet(UUID accountId) {
        return Optional.of(Wallet.of(String.valueOf(UUID.randomUUID()), DEFAULT_CURRENCY, availableBalance, heldBalance));
    }

    @Override
    public void applyHold(UUID idempotencyKey, Wallet wallet, BigDecimal amount) {
        heldBalance = heldBalance.add(amount);
        log.info("Applying hold for walletId: {}, amount: {}", wallet.id(), amount);
    }

    @Override
    public void releaseHold(UUID idempotencyKey, Wallet wallet, BigDecimal amount) {
        log.info("Releasing hold for walletId: {}, amount: {}", wallet.id(), amount);
        heldBalance = heldBalance.subtract(amount);
    }
}
