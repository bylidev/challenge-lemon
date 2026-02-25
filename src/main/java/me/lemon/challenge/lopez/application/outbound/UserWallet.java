package me.lemon.challenge.lopez.application.outbound;

import me.lemon.challenge.lopez.domain.Wallet;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserWallet {
    Optional<Wallet> getDefaultWallet(UUID accountId);

    void applyHold(UUID idempotencyKey, Wallet wallet, BigDecimal amount);

    void releaseHold(UUID idempotencyKey, Wallet wallet, BigDecimal amount);
}
