package me.lemon.challenge.lopez.infrastructure.adapters;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.lemon.challenge.lopez.application.outbound.AuthorizationRepository;
import me.lemon.challenge.lopez.domain.Authorization;
import me.lemon.challenge.lopez.infrastructure.repository.AuthorizationJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.AuthorizationEntity;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthorizationRepositoryAdapter implements AuthorizationRepository {

    private final AuthorizationJpaRepository authorizationJpaRepository;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Authorization save(Authorization authorization) {
        var saved = authorizationJpaRepository.save(map(authorization));
        return map(saved);
    }

    private Authorization map(AuthorizationEntity entity) {
        return new Authorization(
                entity.getId(),
                entity.getAmount(),
                entity.getCardId(),
                entity.getAccountId(),
                entity.getTransactionId(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }

    private AuthorizationEntity map(Authorization authorization) {
        var entity = new AuthorizationEntity();
        entity.setId(authorization.id());
        entity.setTransactionId(authorization.transactionId());
        entity.setAccountId(authorization.accountId());
        entity.setCardId(authorization.cardId());
        entity.setAmount(authorization.amount());
        entity.setStatus(authorization.status());
        entity.setCreatedAt(authorization.timestamp());
        return entity;
    }

    @Override
    public void updateStatusById(UUID id, Authorization.Status status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(status, "status must not be null");
        authorizationJpaRepository.updateStatusById(id, status);
    }
}
