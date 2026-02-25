package me.lemon.challenge.lopez.infrastructure.listeners;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.lemon.challenge.lopez.application.inbound.TradeStatus;
import me.lemon.challenge.lopez.infrastructure.listeners.dto.TradeOutboxMessageDto;


@Slf4j
@RequiredArgsConstructor
@KafkaListener
public class TradeStatusListener {
    private final TradeStatus tradeStatus;

    @Topic("lemon.trades.completed")
    public void complete(TradeOutboxMessageDto messageDto) {
        tradeStatus.onCompleted(messageDto.id());
    }

    @Topic("lemon.trades.failed")
    public void failed(TradeOutboxMessageDto messageDto) {
        tradeStatus.onFailure(messageDto.id());
    }
}
