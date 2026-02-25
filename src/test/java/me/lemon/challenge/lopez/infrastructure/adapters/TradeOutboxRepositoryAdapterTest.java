package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.infrastructure.repository.TradeOutboxJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.TradeOutbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOutboxRepositoryAdapterTest {

    @Mock
    private TradeOutboxJpaRepository tradeOutboxJpaRepository;

    @InjectMocks
    private TradeOutboxRepositoryAdapter tradeOutboxRepositoryAdapter;

    @Test
    void findAuthorizationIdById_shouldMapCorrectly() {
        // Arrange
        UUID tradeId = UUID.randomUUID();
        UUID authorizationId = UUID.randomUUID();
        TradeOutbox tradeOutbox = TradeOutbox.builder()
                .id(tradeId)
                .authorizationId(authorizationId)
                .build();

        when(tradeOutboxJpaRepository.findById(tradeId)).thenReturn(Optional.of(tradeOutbox));

        // Act
        Optional<UUID> result = tradeOutboxRepositoryAdapter.findAuthorizationIdById(tradeId);

        // Assert
        assertEquals(Optional.of(authorizationId), result);
    }
}
