package me.lemon.challenge.lopez.infrastructure.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.TradeOutbox;

import java.util.UUID;

@Repository
public interface TradeOutboxJpaRepository extends JpaRepository<TradeOutbox, UUID> {
}
