package me.lemon.challenge.lopez.infrastructure.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import jakarta.transaction.Transactional;
import me.lemon.challenge.lopez.domain.Authorization;
import me.lemon.challenge.lopez.infrastructure.repository.entity.AuthorizationEntity;

import java.util.UUID;

@Repository
public interface AuthorizationJpaRepository extends JpaRepository<AuthorizationEntity, UUID> {

    @Transactional
    @Query("UPDATE AuthorizationEntity p SET p.status = :status WHERE p.transactionId = :transactionId")
    void updateStatusByTransactionId(String transactionId, Authorization.Status status);

    @Transactional
    @Query("UPDATE AuthorizationEntity p SET p.status = :status WHERE p.id = :id")
    void updateStatusById(UUID id, Authorization.Status status);
}
