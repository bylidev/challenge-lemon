package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.domain.Authorization;
import me.lemon.challenge.lopez.infrastructure.repository.AuthorizationJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.AuthorizationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationRepositoryAdapterTest {

    private AuthorizationJpaRepository authorizationJpaRepository;
    private AuthorizationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        authorizationJpaRepository = mock(AuthorizationJpaRepository.class);
        adapter = new AuthorizationRepositoryAdapter(authorizationJpaRepository);
    }

    @Test
    void save_shouldMapAndPersistEntity() {
        // Arrange
        var authorization = new Authorization(
                null,
                BigDecimal.valueOf(100),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tx-1",
                ZonedDateTime.now(),
                Authorization.Status.PENDING
        );
        var authorizationEntity = new AuthorizationEntity();
        authorizationEntity.setId(UUID.randomUUID());
        authorizationEntity.setAccountId(authorization.accountId());
        authorizationEntity.setCardId(authorization.cardId());
        authorizationEntity.setAmount(authorization.amount());
        authorizationEntity.setTransactionId(authorization.transactionId());
        authorizationEntity.setStatus(authorization.status());
        authorizationEntity.setCreatedAt(authorization.timestamp());

        when(authorizationJpaRepository.save(any())).thenReturn(authorizationEntity);

        // Act
        adapter.save(authorization);

        // Assert
        var captor = ArgumentCaptor.forClass(AuthorizationEntity.class);
        verify(authorizationJpaRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(authorization.id(), entity.getId());
        assertEquals(authorization.transactionId(), entity.getTransactionId());
        assertEquals(authorization.accountId(), entity.getAccountId());
        assertEquals(authorization.cardId(), entity.getCardId());
        assertEquals(authorization.amount(), entity.getAmount());
        assertEquals(authorization.status(), entity.getStatus());
        assertEquals(authorization.timestamp(), entity.getCreatedAt());
    }

    @Test
    void save_shouldThrowExceptionWhenMissingRequiredFields() {
        // Arrange
        assertThrows(NullPointerException.class, () -> new Authorization(null, null, null, null, null, null, null));
    }

    @Test
    void updateStatusById_shouldDelegateToRepository() {
        // Arrange
        var id = UUID.randomUUID();
        // Act
        adapter.updateStatusById(id, Authorization.Status.FAILED);
        // Assert
        verify(authorizationJpaRepository).updateStatusById(id, Authorization.Status.FAILED);
    }

    @Test
    void updateStatusById_shouldThrowExceptionWhenNullArguments() {
        // Arrange, Act & Assert
        NullPointerException e1 = assertThrows(NullPointerException.class, () -> adapter.updateStatusById(null, Authorization.Status.PENDING));
        assertEquals("id must not be null", e1.getMessage());
        NullPointerException e2 = assertThrows(NullPointerException.class, () -> adapter.updateStatusById(UUID.randomUUID(), null));
        assertEquals("status must not be null", e2.getMessage());
    }
}