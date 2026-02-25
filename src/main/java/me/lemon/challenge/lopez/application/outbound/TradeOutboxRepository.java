package me.lemon.challenge.lopez.application.outbound;

import java.util.Optional;
import java.util.UUID;

public interface TradeOutboxRepository {
    Optional<UUID> findAuthorizationIdById(UUID tradeId);
}
