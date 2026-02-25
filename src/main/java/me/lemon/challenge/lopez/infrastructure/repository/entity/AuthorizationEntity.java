package me.lemon.challenge.lopez.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.lemon.challenge.lopez.domain.Authorization;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "authorizations")
@Getter
@Setter
@NoArgsConstructor
public class AuthorizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "amount_ars", nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status")
    private Authorization.Status status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

}
