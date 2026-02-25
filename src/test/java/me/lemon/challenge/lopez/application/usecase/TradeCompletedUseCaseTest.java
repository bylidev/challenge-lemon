package me.lemon.challenge.lopez.application.usecase;

import me.lemon.challenge.lopez.application.outbound.AuthorizationRepository;
import me.lemon.challenge.lopez.application.outbound.TradeOutboxRepository;
import me.lemon.challenge.lopez.domain.Authorization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeCompletedUseCaseTest {

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private TradeOutboxRepository tradeOutboxRepository;

    @InjectMocks
    private TradeCompletedUseCase tradeCompletedUseCase;

    @Test
    void shouldUpdateStatusToCompleted_whenTradeIsCompletedAndAuthorizationExists() {
        // Arrange
        UUID tradeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        when(tradeOutboxRepository.findAuthorizationIdById(tradeId))
                .thenReturn(Optional.of(authId));

        // Act
        tradeCompletedUseCase.onCompleted(tradeId);

        // Assert
        verify(authorizationRepository).updateStatusById(authId, Authorization.Status.COMPLETED);
        verify(tradeOutboxRepository).findAuthorizationIdById(tradeId);
    }

    @Test
    void shouldUpdateStatusToFailed_whenTradeFailsAndAuthorizationExists() {
        // Arrange
        UUID tradeId = UUID.randomUUID();
        UUID authId = UUID.randomUUID();

        when(tradeOutboxRepository.findAuthorizationIdById(tradeId))
                .thenReturn(Optional.of(authId));

        // Act
        tradeCompletedUseCase.onFailure(tradeId);

        // Assert
        verify(authorizationRepository).updateStatusById(authId, Authorization.Status.FAILED);
    }

    @Test
    void shouldDoNothing_whenAuthorizationIdNotFound() {
        // Arrange
        UUID tradeId = UUID.randomUUID();
        when(tradeOutboxRepository.findAuthorizationIdById(tradeId))
                .thenReturn(Optional.empty());

        // Act
        tradeCompletedUseCase.onCompleted(tradeId);

        // Assert
        verifyNoInteractions(authorizationRepository);
    }
}