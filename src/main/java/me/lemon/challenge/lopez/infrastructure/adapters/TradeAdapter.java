package me.lemon.challenge.lopez.infrastructure.adapters;

import lombok.RequiredArgsConstructor;
import me.lemon.challenge.lopez.application.outbound.Trade;
import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;
import me.lemon.challenge.lopez.infrastructure.repository.TradeOutboxJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.TradeOutbox;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class TradeAdapter implements Trade {
    private final TradeOutboxJpaRepository tradeOutboxJpaRepository;

    @Override
    public void buy(UUID authorizationId, BigDecimal amount, Currency currency, Wallet wallet) {
        var outbox = TradeOutbox.builder()
                .authorizationId(authorizationId)
                .walletId(wallet.id())
                .quoteCurrency(currency.name())
                .amount(amount)
                .operationType(TradeOutbox.OperationType.BUY)
                .createdAt(ZonedDateTime.now())
                .build();
        tradeOutboxJpaRepository.save(outbox);
    }
}
