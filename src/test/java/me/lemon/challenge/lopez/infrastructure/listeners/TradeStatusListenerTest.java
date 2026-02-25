package me.lemon.challenge.lopez.infrastructure.listeners;

import me.lemon.challenge.lopez.application.inbound.TradeStatus;
import me.lemon.challenge.lopez.infrastructure.listeners.dto.TradeOutboxMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TradeStatusListenerTest {

    @Mock
    private TradeStatus tradeStatus;

    @InjectMocks
    private TradeStatusListener tradeStatusListener;

    private TradeOutboxMessageDto messageDto;
    private UUID tradeId;

    @BeforeEach
    void setUp() {
        tradeId = UUID.randomUUID();
        messageDto = new TradeOutboxMessageDto(tradeId);
    }

    @Test
    void shouldCallOnCompletedWhenMessageIsReceived() {
        // Act
        tradeStatusListener.complete(messageDto);

        // Assert
        verify(tradeStatus, times(1)).onCompleted(tradeId);
    }

    @Test
    void shouldCallOnFailureWhenMessageIsReceived() {
        // Act
        tradeStatusListener.failed(messageDto);

        // Assert
        verify(tradeStatus, times(1)).onFailure(tradeId);
    }
}