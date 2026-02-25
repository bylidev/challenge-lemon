package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.application.outbound.ExchangeRate;
import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Pair;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static me.lemon.challenge.lopez.domain.Currency.*;

/**
 * Note: Suppress warnings for this mock.
 * Used for demonstration purposes only.
 */
public class MockedExchangeRateAdapter implements ExchangeRate {
    private static final Map<Pair, BigDecimal> MOCKED_RATES = Map.of(
            Pair.of(ARS, USDT), new BigDecimal("1450"),
            Pair.of(BTC, USDT), new BigDecimal("67500"),
            Pair.of(ETH, USDT), new BigDecimal("1950")
    );

    @Override
    public BigDecimal getExchangeRate(Currency base, Currency quote) {
        return Optional.of(MOCKED_RATES)
                .map(m -> m.get(Pair.of(base, quote)))
                .orElseThrow(RuntimeException::new);

    }
}
