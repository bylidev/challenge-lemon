package me.lemon.challenge.lopez.application.outbound;

import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface Trade {
    void buy(UUID idempotencyKey, BigDecimal amount, Currency currency, Wallet wallet);
}
