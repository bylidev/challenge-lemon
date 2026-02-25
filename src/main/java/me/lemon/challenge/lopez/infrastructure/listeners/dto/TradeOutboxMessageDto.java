package me.lemon.challenge.lopez.infrastructure.listeners.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public record TradeOutboxMessageDto(UUID id) {
}
