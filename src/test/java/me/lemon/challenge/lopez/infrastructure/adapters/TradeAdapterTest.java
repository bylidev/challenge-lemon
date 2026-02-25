package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;
import me.lemon.challenge.lopez.infrastructure.repository.TradeOutboxJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.entity.TradeOutbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TradeAdapterTest {

    @Mock
    private TradeOutboxJpaRepository repository;

    @InjectMocks
    private TradeAdapter tradeAdapter;

    @Test
    void shouldSaveTradeInOutboxWhenBuying() {
        // Arrange
        UUID authId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("0.5");
        Currency currency = Currency.BTC;

        Wallet wallet = Wallet.of(walletId.toString(), Currency.USDT, BigDecimal.valueOf(100), BigDecimal.ZERO);

        // Act
        tradeAdapter.buy(authId, amount, currency, wallet);

        // Assert
        ArgumentCaptor<TradeOutbox> captor = ArgumentCaptor.forClass(TradeOutbox.class);
        verify(repository, times(1)).save(captor.capture());

        TradeOutbox savedEntity = captor.getValue();
        assertEquals(authId, savedEntity.getAuthorizationId());
        assertEquals(walletId.toString(), savedEntity.getWalletId());
        assertEquals("BTC", savedEntity.getQuoteCurrency());
        assertEquals(amount, savedEntity.getAmount());
        assertEquals(TradeOutbox.OperationType.BUY, savedEntity.getOperationType());
    }
}