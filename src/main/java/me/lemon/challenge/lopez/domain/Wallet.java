package me.lemon.challenge.lopez.domain;

import java.math.BigDecimal;

public record Wallet(
        String id,
        Currency currency,
        BigDecimal available,
        BigDecimal held) {

    public static Wallet of(String id, Currency currency,
                            BigDecimal available, BigDecimal held) {
        if (held.compareTo(available) > 0) {
            throw new IllegalArgumentException("held > available");
        }
        return new Wallet(id, currency, available, held);
    }

    public BigDecimal total() {
        return available.add(held);
    }
}
