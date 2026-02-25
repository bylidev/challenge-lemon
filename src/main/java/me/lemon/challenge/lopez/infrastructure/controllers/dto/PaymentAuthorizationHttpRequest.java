package me.lemon.challenge.lopez.infrastructure.controllers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;

@Serdeable
public record PaymentAuthorizationHttpRequest(
        BigDecimal amount,
        @JsonProperty("card_id") String cardId,
        @JsonProperty("account_id") String accountId,
        @JsonProperty("transaction_id") String transactionId
) {
}
