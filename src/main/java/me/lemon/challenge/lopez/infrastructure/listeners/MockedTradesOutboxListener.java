package me.lemon.challenge.lopez.infrastructure.listeners;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.lemon.challenge.lopez.infrastructure.listeners.dto.TradeOutboxMessageDto;

import java.util.UUID;


/**
 * Note: Mock for demonstration purposes only.
 * This is a mock to demonstrate the outbox pattern functionality.
 */
@Slf4j
@RequiredArgsConstructor
@KafkaListener
public class MockedTradesOutboxListener {

    private final ObjectMapper objectMapper;
    private final TradesCompletedClient kafkaClient;

    @Topic("lemon.public.trades_outbox")
    public void receive(String jsonMessage) {
        try {
            var rootNode = objectMapper.readValue(jsonMessage, JsonNode.class);
            var payloadNode = rootNode.get("payload");
            var payload = objectMapper.readValueFromTree(payloadNode, JsonNode.class);
            var tradeId = UUID.fromString(payload.get("id").getStringValue());
            kafkaClient.send(new TradeOutboxMessageDto(tradeId));
        } catch (Exception e) {
            log.error("Error inesperado procesando el mensaje", e);
        }
    }

    @KafkaClient
    interface TradesCompletedClient {
        @Topic("lemon.trades.completed")
        void send(TradeOutboxMessageDto body);
    }
}