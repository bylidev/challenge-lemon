package me.lemon.challenge.lopez.infrastructure.configuration;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.application.inbound.TradeStatus;
import me.lemon.challenge.lopez.application.outbound.*;
import me.lemon.challenge.lopez.application.usecase.PaymentAuthorizationUseCase;
import me.lemon.challenge.lopez.application.usecase.TradeCompletedUseCase;
import me.lemon.challenge.lopez.domain.Currency;
import me.lemon.challenge.lopez.infrastructure.adapters.*;
import me.lemon.challenge.lopez.infrastructure.decorators.TransactionalPaymentAuthorizationDecorator;
import me.lemon.challenge.lopez.infrastructure.facades.DomainIdMapperFacade;
import me.lemon.challenge.lopez.infrastructure.facades.MockedAuthorizationIdConverterFacade;
import me.lemon.challenge.lopez.infrastructure.repository.AuthorizationJpaRepository;
import me.lemon.challenge.lopez.infrastructure.repository.TradeOutboxJpaRepository;

import java.math.BigDecimal;

@Factory
public class ApplicationFactory {

    @Value("${payment.volatility-buffer:1.05}")
    BigDecimal volatilityBuffer;
    @Value("${payment.crypto-trade-commission:1.01}")
    BigDecimal cryptoTradeCommission;
    @Value("${payment.base-currency:ARS}")
    Currency baseCurrency;

    @Singleton
    public UserWallet userWallet() {
        return new MockedUserWalletAdapter();
    }

    @Singleton
    public ExchangeRate exchangeRate() {
        return new MockedExchangeRateAdapter();
    }

    @Singleton
    public CardValidation cardValidation() {
        return new MockedCardValidationAdapter();
    }

    @Singleton
    public AuthorizationRepository authorizationRepository(AuthorizationJpaRepository authorizationJpaRepository) {
        return new AuthorizationRepositoryAdapter(authorizationJpaRepository);
    }

    @Singleton
    public Trade trade(TradeOutboxJpaRepository tradeOutboxJpaRepository) {
        return new TradeAdapter(tradeOutboxJpaRepository);
    }

    @Singleton
    public TradeStatus tradeStatus(AuthorizationRepository authorizationRepository, TradeOutboxRepository tradeOutboxRepository) {
        return new TradeCompletedUseCase(authorizationRepository, tradeOutboxRepository);
    }

    @Singleton
    public TradeOutboxRepository tradeOutboxRepository(TradeOutboxJpaRepository tradeOutboxJpaRepository) {
        return new TradeOutboxRepositoryAdapter(tradeOutboxJpaRepository);
    }

    @Singleton
    public DomainIdMapperFacade domainIdMapperFacade() {
        return new MockedAuthorizationIdConverterFacade();
    }

    @Singleton
    public ObjectMapper objectMapper() {
        return ObjectMapper.getDefault();
    }

    @Singleton
    public PaymentAuthorization payment(UserWallet userWallet, ExchangeRate exchangeRate, CardValidation cardValidation,
                                        Trade trade, AuthorizationRepository authorizationRepository) {
        return new TransactionalPaymentAuthorizationDecorator(
                new PaymentAuthorizationUseCase(userWallet, exchangeRate, cardValidation, trade, volatilityBuffer,
                        cryptoTradeCommission, authorizationRepository, baseCurrency)
        );
    }
}
