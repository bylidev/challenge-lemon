package me.lemon.challenge.lopez.infrastructure.adapters;

import lombok.RequiredArgsConstructor;
import me.lemon.challenge.lopez.application.outbound.TradeOutboxRepository;
import me.lemon.challenge.lopez.infrastructure.repository.TradeOutboxJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.TradeOutbox;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TradeOutboxRepositoryAdapter implements TradeOutboxRepository {
    private final TradeOutboxJpaRepository tradeOutboxJpaRepository;

    @Override
    public Optional<UUID> findAuthorizationIdById(UUID tradeId) {
        return tradeOutboxJpaRepository.findById(tradeId)
                .map(TradeOutbox::getAuthorizationId);
    }
}
