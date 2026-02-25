package me.lemon.challenge.lopez.application.usecase;

import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.application.outbound.*;
import me.lemon.challenge.lopez.domain.Authorization;
import me.lemon.challenge.lopez.domain.Wallet;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static me.lemon.challenge.lopez.domain.Currency.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorizationUseCaseTest {

    @Mock
    private UserWallet userWallet;
    @Mock
    private ExchangeRate exchangeRate;
    @Mock
    private Trade trade;
    @Mock
    private CardValidation cardValidation;
    @Mock
    private AuthorizationRepository authorizationRepository;

    private PaymentAuthorizationUseCase useCase;

    private static final UUID CARD_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String TRANSACTION_ID = "trx_abcdef123456";

    private static final BigDecimal PURCHASE_AMOUNT_ARS = new BigDecimal("15000.50");
    private static final BigDecimal VOLATILITY_BUFFER = new BigDecimal("1.015");
    private static final BigDecimal CRYPTO_TRADE_COMMISSION = new BigDecimal("1.005");

    private static final BigDecimal ARS_RATE = new BigDecimal("1450");
    private static final BigDecimal BTC_RATE = new BigDecimal("67500");
    private static final BigDecimal ETH_RATE = new BigDecimal("1950");

    private static final Wallet ARS_WALLET = Wallet.of("wlt_ars_001", ARS, new BigDecimal("20000.00"), BigDecimal.ZERO);
    private static final Wallet USDT_WALLET = Wallet.of("wlt_usdt_001", USDT, new BigDecimal("20.00000000"), BigDecimal.ZERO);
    private static final Wallet BTC_WALLET = Wallet.of("wlt_btc_001", BTC, new BigDecimal("1.00000000"), BigDecimal.ZERO);
    private static final Wallet ETH_WALLET = Wallet.of("wlt_eth_001", ETH, new BigDecimal("10.00000000"), BigDecimal.ZERO);

    private static final int ROUNDING_SCALE = 18;

    @BeforeEach
    void setUp() {
        useCase = new PaymentAuthorizationUseCase(userWallet, exchangeRate, cardValidation, trade, VOLATILITY_BUFFER, CRYPTO_TRADE_COMMISSION, authorizationRepository, ARS);
    }

    @Test
    void shouldAuthorizePayment_whenDefaultWalletIsARS() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization(); // Obtenemos la auth con su ID
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ARS_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act
        useCase.authorize(defaultRequest());

        // Assert
        var holdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        // FIX: Usamos el ID de la autorización y el objeto ARS_WALLET completo
        verify(userWallet).applyHold(eq(savedAuth.id()), eq(ARS_WALLET), holdCaptor.capture());
        assertEquals(0, PURCHASE_AMOUNT_ARS.compareTo(holdCaptor.getValue()));
        verifyNoInteractions(trade);
    }

    @Test
    void shouldAuthorizePayment_holdExactlyOneUSDTWithoutCommission_whenDefaultWalletIsUSDT() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization();
        var purchaseInArs = new BigDecimal("1450");
        var request = new AuthorizationRequest(purchaseInArs, CARD_ID, ACCOUNT_ID, TRANSACTION_ID, ZonedDateTime.now());

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(USDT_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act
        useCase.authorize(request);

        // Assert
        var holdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        // FIX: Usamos el ID de la autorización y USDT_WALLET
        verify(userWallet).applyHold(eq(savedAuth.id()), eq(USDT_WALLET), holdCaptor.capture());
        assertEquals(0, new BigDecimal("1.00000000").compareTo(holdCaptor.getValue()));
        verifyNoInteractions(trade);
    }

    @Test
    void shouldAuthorizePayment_whenDefaultWalletIsBTC() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);

        // Act
        useCase.authorize(defaultRequest());

        // Assert
        var usdAmount = PURCHASE_AMOUNT_ARS.divide(ARS_RATE, ROUNDING_SCALE, RoundingMode.UP);
        var expectedBtcHold = usdAmount.divide(BTC_RATE, ROUNDING_SCALE, RoundingMode.UP)
                .multiply(VOLATILITY_BUFFER).multiply(CRYPTO_TRADE_COMMISSION);

        var holdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        // FIX: Verificamos applyHold con los nuevos parámetros
        verify(userWallet).applyHold(eq(savedAuth.id()), eq(BTC_WALLET), holdCaptor.capture());
        assertEquals(0, expectedBtcHold.compareTo(holdCaptor.getValue()));

        verify(authorizationRepository).updateStatusById(eq(savedAuth.id()), eq(Authorization.Status.AUTHORIZED));

        // El trade ya usaba el ID, así que esto debería mantenerse similar
        verify(trade).buy(eq(savedAuth.id()), any(BigDecimal.class), eq(USDT), eq(BTC_WALLET));
    }

    @Test
    void shouldAuthorizePayment_whenDefaultWalletIsETH() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ETH_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(ETH, USDT)).thenReturn(ETH_RATE);

        // Act
        useCase.authorize(defaultRequest());

        // Assert
        var usdAmount = PURCHASE_AMOUNT_ARS.divide(ARS_RATE, ROUNDING_SCALE, RoundingMode.UP);
        var expectedEthHold = usdAmount.divide(ETH_RATE, ROUNDING_SCALE, RoundingMode.UP)
                .multiply(VOLATILITY_BUFFER).multiply(CRYPTO_TRADE_COMMISSION);

        var holdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userWallet).applyHold(eq(savedAuth.id()), eq(ETH_WALLET), holdCaptor.capture());
        assertEquals(0, expectedEthHold.compareTo(holdCaptor.getValue()));

        verify(authorizationRepository).updateStatusById(eq(savedAuth.id()), eq(Authorization.Status.AUTHORIZED));
        verify(trade).buy(eq(savedAuth.id()), any(BigDecimal.class), eq(USDT), eq(ETH_WALLET));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenCardIsInvalid() {
        // Arrange
        var invalidCardId = UUID.randomUUID();
        var request = new AuthorizationRequest(PURCHASE_AMOUNT_ARS, invalidCardId, ACCOUNT_ID, TRANSACTION_ID, ZonedDateTime.now());
        when(cardValidation.isValid(argThat(id -> !CARD_ID.equals(id)))).thenReturn(false);

        // Act & Assert
        var exception = assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(request));
        assertTrue(exception.getMessage().contains("invalid") || exception.getMessage().contains("blocked"));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenAccountDoesNotExist() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
        assertTrue(exception.getMessage().contains(ACCOUNT_ID.toString()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenInsufficientFundsOnARSWallet() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_ars_001", ARS, new BigDecimal("10000.00"), BigDecimal.ZERO);

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenInsufficientFundsOnUSDTWallet() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_usdt_001", USDT, new BigDecimal("5.00000000"), BigDecimal.ZERO);

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenInsufficientFundsOnBTCWallet() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_btc_001", BTC, new BigDecimal("0.0001"), BigDecimal.ZERO);

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenInsufficientFundsOnETHWallet() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_eth_001", ETH, new BigDecimal("0.005"), BigDecimal.ZERO);

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(ETH, USDT)).thenReturn(ETH_RATE);

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenAllFundsAreHeld() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_ars_001", ARS, BigDecimal.valueOf(50), BigDecimal.valueOf(50));
        var request = new AuthorizationRequest(BigDecimal.valueOf(50), CARD_ID, ACCOUNT_ID, TRANSACTION_ID, ZonedDateTime.now());

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act & Assert
        var exception = assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(request));
        assertEquals("Insufficient available funds", exception.getMessage());
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenARSRateIsMissingForARSWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ARS_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenARSRateIsMissingForUSDTWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(USDT_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenARSRateIsMissingForBTCWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenARSRateIsMissingForETHWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ETH_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenCryptoRateIsMissingForBTCWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenCryptoRateIsMissingForETHWallet() {
        // Arrange
        givenCardIsValid();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ETH_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(ETH, USDT)).thenThrow(new PaymentUnauthorizedException("Rate not available"));

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldNotReleaseHold_andShouldMarkPaymentAsFailed_whenApplyHoldFails() {
        // Arrange
        givenCardIsValid();
        givenSaveReturnsNewAuthorization();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ARS_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        doThrow(new PaymentUnauthorizedException("wallet service unavailable")).when(userWallet).applyHold(any(), any(), any());

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
        // funds were never held (applyHold threw before heldFunds=true), so releaseHold must not be called
        verify(userWallet, never()).releaseHold(any(), any(), any());
        // status must still be persisted as FAILED via finalizeFailureStatus
        verify(authorizationRepository).updateStatusById(any(UUID.class), eq(Authorization.Status.FAILED));
    }

    @Test
    void shouldThrowPaymentUnauthorizedException_whenTradeOutboxFails() {
        // Arrange
        givenCardIsValid();
        givenSaveReturnsNewAuthorization();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);
        doThrow(new PaymentUnauthorizedException("trade service unavailable")).when(trade).buy(any(), any(), any(), any());

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
    }

    @Test
    void shouldReleaseHold_whenTradeFails() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization(); // Obtenemos la auth para usar su ID
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);
        doThrow(new PaymentUnauthorizedException("trade service unavailable")).when(trade).buy(any(), any(), any(), any());

        // Act
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));

        // Assert
        var usdAmount = PURCHASE_AMOUNT_ARS.divide(ARS_RATE, ROUNDING_SCALE, RoundingMode.UP);
        var expectedHold = usdAmount.divide(BTC_RATE, ROUNDING_SCALE, RoundingMode.UP)
                .multiply(VOLATILITY_BUFFER).multiply(CRYPTO_TRADE_COMMISSION);

        var releaseCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(userWallet).releaseHold(eq(savedAuth.id()), eq(BTC_WALLET), releaseCaptor.capture());
        assertEquals(0, expectedHold.compareTo(releaseCaptor.getValue()));
    }

    @Test
    void shouldSavePaymentBeforeHoldingFundsAndThenAuthorize_whenDefaultWalletIsARS() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization();
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(ARS_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act
        useCase.authorize(defaultRequest());

        // Assert
        var inOrder = inOrder(authorizationRepository, userWallet);
        var paymentCaptor = ArgumentCaptor.forClass(Authorization.class);
        inOrder.verify(authorizationRepository).save(paymentCaptor.capture());
        assertEquals(Authorization.Status.PENDING, paymentCaptor.getValue().status());
        inOrder.verify(userWallet).applyHold(eq(savedAuth.id()), eq(ARS_WALLET), any(BigDecimal.class));
        inOrder.verify(authorizationRepository).updateStatusById(eq(savedAuth.id()), eq(Authorization.Status.AUTHORIZED));
    }

    @Test
    void shouldSavePaymentBeforeHoldingFundsThenTradeAndThenAuthorize_whenDefaultWalletIsBTC() {
        // Arrange
        givenCardIsValid();
        Authorization savedAuth = givenSaveReturnsNewAuthorization();

        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);

        // Act
        useCase.authorize(defaultRequest());

        // Assert
        var inOrder = inOrder(authorizationRepository, userWallet, trade);
        inOrder.verify(authorizationRepository).save(any(Authorization.class));
        inOrder.verify(userWallet).applyHold(eq(savedAuth.id()), eq(BTC_WALLET), any(BigDecimal.class));
        inOrder.verify(trade).buy(eq(savedAuth.id()), any(BigDecimal.class), eq(USDT), eq(BTC_WALLET));
        inOrder.verify(authorizationRepository).updateStatusById(eq(savedAuth.id()), eq(Authorization.Status.AUTHORIZED));
    }

    @Test
    void shouldNotSavePayment_whenInsufficientFunds() {
        // Arrange
        givenCardIsValid();
        var wallet = Wallet.of("wlt_ars_001", ARS, new BigDecimal("10000.00"), BigDecimal.ZERO);
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(wallet));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);

        // Act & Assert
        assertThrows(PaymentUnauthorizedException.class, () -> useCase.authorize(defaultRequest()));
        verify(authorizationRepository, never()).save(any(Authorization.class));
    }

    @Test
    void shouldThrowIllegalArgumentException_whenVolatilityBufferIsLessThanOne() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentAuthorizationUseCase(userWallet, exchangeRate, cardValidation, trade,
                        new BigDecimal("0.99"), CRYPTO_TRADE_COMMISSION, authorizationRepository, ARS));
    }

    @Test
    void shouldThrowIllegalArgumentException_whenCryptoTradeCommissionIsLessThanOne() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentAuthorizationUseCase(userWallet, exchangeRate, cardValidation, trade,
                        VOLATILITY_BUFFER, new BigDecimal("0.99"), authorizationRepository, ARS));
    }

    @Test
    void shouldSwallowUpdateFailedStatusException_andStillNotifyMetrics_whenUpdateStatusToFailedThrows() {
        // Arrange
        givenCardIsValid();
        givenSaveReturnsNewAuthorization();
        var tradeException = new RuntimeException("trade service unavailable");
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);
        doThrow(tradeException).when(trade).buy(any(), any(), any(), any());
        doThrow(new RuntimeException("db connection lost")).when(authorizationRepository).updateStatusById(any(UUID.class), eq(Authorization.Status.FAILED));

        // Act & Assert: original trade exception propagates even though status update to FAILED also threw
        var thrown = assertThrows(RuntimeException.class, () -> useCase.authorize(defaultRequest()));
        assertSame(tradeException, thrown);
    }

    @Test
    void shouldRethrowAndMarkPaymentAsFailed_whenReleasingHoldFailsDuringFailureRecovery() {
        // Arrange
        givenCardIsValid();
        givenSaveReturnsNewAuthorization();
        var releaseException = new RuntimeException("wallet service down");
        when(userWallet.getDefaultWallet(ACCOUNT_ID)).thenReturn(Optional.of(BTC_WALLET));
        when(exchangeRate.getExchangeRate(ARS, USDT)).thenReturn(ARS_RATE);
        when(exchangeRate.getExchangeRate(BTC, USDT)).thenReturn(BTC_RATE);
        doThrow(new RuntimeException("trade service unavailable")).when(trade).buy(any(), any(), any(), any());
        doThrow(releaseException).when(userWallet).releaseHold(any(), any(), any());

        // Act & Assert
        var thrown = assertThrows(RuntimeException.class, () -> useCase.authorize(defaultRequest()));
        assertSame(releaseException, thrown);
        verify(authorizationRepository).updateStatusById(any(UUID.class), eq(Authorization.Status.FAILED));
    }

    private void givenCardIsValid() {
        when(cardValidation.isValid(CARD_ID)).thenReturn(true);
    }

    private AuthorizationRequest defaultRequest() {
        return new AuthorizationRequest(PURCHASE_AMOUNT_ARS, CARD_ID, ACCOUNT_ID, TRANSACTION_ID, ZonedDateTime.now());
    }

    private Authorization givenSaveReturnsNewAuthorization() {
        Authorization auth = new Authorization(
                UUID.randomUUID(),
                PURCHASE_AMOUNT_ARS,
                CARD_ID,
                ACCOUNT_ID,
                TRANSACTION_ID,
                ZonedDateTime.now(),
                Authorization.Status.PENDING
        );
        when(authorizationRepository.save(any(Authorization.class))).thenReturn(auth);
        return auth;
    }
}
