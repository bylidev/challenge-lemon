package me.lemon.challenge.lopez.application.inbound.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record AuthorizationRequest(BigDecimal amount, UUID cardId, UUID accountId, String transactionId,
                                   ZonedDateTime transactionDate) {

    public AuthorizationRequest {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(cardId, "cardId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
    }
}

