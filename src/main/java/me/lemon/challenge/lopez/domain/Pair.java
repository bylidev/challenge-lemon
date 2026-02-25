package me.lemon.challenge.lopez.domain;

import java.util.Objects;

public record Pair(Currency base, Currency quote) {
    public Pair {
        Objects.requireNonNull(base, "base currency must not be null");
        Objects.requireNonNull(quote, "quote currency must not be null");
    }

    public static Pair of(Currency base, Currency quote) {
        return new Pair(base, quote);
    }
}
