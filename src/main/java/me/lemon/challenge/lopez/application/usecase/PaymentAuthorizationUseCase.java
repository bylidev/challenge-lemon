package me.lemon.challenge.lopez.application.usecase;

import lombok.extern.slf4j.Slf4j;
import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.application.inbound.dto.AuthorizationRequest;
import me.lemon.challenge.lopez.application.outbound.*;
import me.lemon.challenge.lopez.domain.Authorization;
import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.domain.Wallet;
import me.lemon.challenge.lopez.domain.exceptions.PaymentUnauthorizedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static me.lemon.challenge.lopez.domain.Currency.USDT;

@Slf4j
public class PaymentAuthorizationUseCase implements PaymentAuthorization {

    private static final int ROUNDING_SCALE = 18;

    private final UserWallet userWallet;
    private final ExchangeRate exchangeRate;
    private final CardValidation cardValidation;
    private final Trade trade;
    private final BigDecimal volatilityBuffer;
    private final BigDecimal cryptoTradeCommission;
    private final AuthorizationRepository authorizationRepository;
    private final Currency baseCurrency;

    public PaymentAuthorizationUseCase(UserWallet userWallet, ExchangeRate exchangeRate, CardValidation cardValidation, Trade trade, BigDecimal volatilityBuffer, BigDecimal cryptoTradeCommission, AuthorizationRepository authorizationRepository, Currency baseCurrency) {
        this.userWallet = userWallet;
        this.exchangeRate = exchangeRate;
        this.cardValidation = cardValidation;
        this.trade = trade;
        this.volatilityBuffer = volatilityBuffer;
        this.cryptoTradeCommission = cryptoTradeCommission;
        this.authorizationRepository = authorizationRepository;
        this.baseCurrency = baseCurrency;

        validateArgs();

    }

    private void validateArgs() {
        if (volatilityBuffer.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Volatility buffer must be >= 1");
        }
        if (cryptoTradeCommission.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Crypto trade commission must be >= 1");
        }
    }

    @Override
    public void authorize(AuthorizationRequest request) {
        validateCard(request.cardId());

        Wallet wallet = getDefaultWallet(request.accountId());

        BigDecimal baseToUsdRate = exchangeRate.getExchangeRate(baseCurrency, USDT);
        BigDecimal usdAmount = request.amount().divide(baseToUsdRate, ROUNDING_SCALE, RoundingMode.UP);
        BigDecimal amountToHold = determineAmountToHold(wallet, request.amount(), usdAmount);

        ensureSufficientBalance(wallet.available().subtract(wallet.held()), amountToHold);

        processAuthorization(wallet, request, amountToHold, usdAmount);
    }

    private void processAuthorization(Wallet wallet, AuthorizationRequest request, BigDecimal amountToHold, BigDecimal usdAmount) {
        Authorization authorization = savePendingPayment(request);
        var heldFunds = false;
        try {
            userWallet.applyHold(authorization.id(), wallet, amountToHold);
            heldFunds = true;
            executeTradeIfRequired(authorization.id(), wallet, usdAmount);
            updatePaymentStatus(authorization.id(), Authorization.Status.AUTHORIZED);
        } catch (Exception e) {
            handleFailure(authorization, wallet, amountToHold, heldFunds);
            throw e;
        }
    }

    private void validateCard(UUID cardId) {
        if (!cardValidation.isValid(cardId)) {
            throw new PaymentUnauthorizedException("Card is invalid or blocked");
        }
    }

    private Wallet getDefaultWallet(UUID accountId) {
        return userWallet.getDefaultWallet(accountId)
                .orElseThrow(() -> new PaymentUnauthorizedException("Account not found: " + accountId));
    }

    private void executeTradeIfRequired(UUID authorizationId, Wallet wallet, BigDecimal usdAmount) {
        if (wallet.currency() != baseCurrency && wallet.currency() != USDT) {
            trade.buy(authorizationId, usdAmount, USDT, wallet);
        }
    }

    private void handleFailure(Authorization auth, Wallet wallet, BigDecimal amount, boolean wasHeld) {
        log.error("Handling failure for authorization {}. Funds were held: {}", auth.id(), wasHeld);
        try {
            if (wasHeld) {
                releaseFundsHold(auth.id(), wallet, amount);
            }
        } finally {
            finalizeFailureStatus(auth);
        }
    }

    private void finalizeFailureStatus(Authorization auth) {
        try {
            updatePaymentStatus(auth.id(), Authorization.Status.FAILED);
        } catch (Exception e) {
            log.error("Could not update status to FAILED for auth {}", auth.id(), e);
        }
    }

    private BigDecimal determineAmountToHold(Wallet wallet, BigDecimal baseAmount, BigDecimal usdAmount) {
        if (wallet.currency() == baseCurrency) return baseAmount;
        if (wallet.currency() == USDT) return usdAmount;

        BigDecimal cryptoToUsdRate = exchangeRate.getExchangeRate(wallet.currency(), USDT);
        return usdAmount.divide(cryptoToUsdRate, ROUNDING_SCALE, RoundingMode.UP)
                .multiply(volatilityBuffer)
                .multiply(cryptoTradeCommission);
    }

    private void ensureSufficientBalance(BigDecimal available, BigDecimal required) {
        if (available.compareTo(required) < 0) {
            throw new PaymentUnauthorizedException("Insufficient available funds");
        }
    }

    private void releaseFundsHold(UUID accountId, Wallet wallet, BigDecimal amount) {
        userWallet.releaseHold(accountId, wallet, amount);
    }

    private Authorization savePendingPayment(AuthorizationRequest request) {
        Authorization authorization = new Authorization(null, request.amount(), request.cardId(),
                request.accountId(), request.transactionId(), request.transactionDate(), Authorization.Status.PENDING);
        return authorizationRepository.save(authorization);
    }

    private void updatePaymentStatus(UUID id, Authorization.Status status) {
        authorizationRepository.updateStatusById(id, status);
    }
}