package me.lemon.challenge.lopez.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static me.lemon.challenge.lopez.domain.Currency.*;

class PairTest {
    @Test
    void of_createsPairWithBaseAndQuote() {
        var pair = Pair.of(BTC, USDT);
        assertEquals(BTC, pair.base());
        assertEquals(USDT, pair.quote());
    }

    @Test
    void record_equalityAndHashCode() {
        var pair1 = Pair.of(ETH, USD);
        var pair2 = Pair.of(ETH, USD);
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    void notNullValidation() {
        assertThrows(NullPointerException.class, () -> Pair.of(null, USD));
        assertThrows(NullPointerException.class, () -> Pair.of(BTC, null));
    }
}

