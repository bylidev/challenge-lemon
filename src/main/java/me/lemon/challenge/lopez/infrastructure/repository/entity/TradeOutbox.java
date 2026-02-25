package me.lemon.challenge.lopez.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Table(name = "trades_outbox")
public class TradeOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "authorization_id", nullable = false, unique = true)
    private UUID authorizationId;

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(name = "quote_currency", nullable = false)
    private String quoteCurrency;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private OperationType operationType;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    public enum OperationType {
        BUY
    }

}
