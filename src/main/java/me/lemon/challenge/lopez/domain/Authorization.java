package me.lemon.challenge.lopez.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public record Authorization(
        UUID id,
        BigDecimal amount,
        UUID cardId,
        UUID accountId,
        String transactionId,
        ZonedDateTime timestamp,
        Status status
) {
    public Authorization {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(cardId, "cardId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(transactionId, "transactionId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public enum Status {
        PENDING,
        AUTHORIZED,
        FAILED,
        COMPLETED
    }
}
