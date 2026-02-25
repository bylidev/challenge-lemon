package me.lemon.challenge.lopez.application.outbound;

import me.lemon.challenge.lopez.domain.Currency;

import java.math.BigDecimal;

public interface ExchangeRate {
    BigDecimal getExchangeRate(Currency base, Currency quote);
}
