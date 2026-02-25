package me.lemon.challenge.lopez.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.lemon.challenge.lopez.application.inbound.TradeStatus;
import me.lemon.challenge.lopez.application.outbound.AuthorizationRepository;
import me.lemon.challenge.lopez.application.outbound.TradeOutboxRepository;
import me.lemon.challenge.lopez.domain.Authorization;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TradeCompletedUseCase implements TradeStatus {
    private final AuthorizationRepository authorizationRepository;
    private final TradeOutboxRepository tradeOutboxRepository;

    @Override
    public void onCompleted(UUID tradeId) {
        tradeOutboxRepository.findAuthorizationIdById(tradeId).ifPresent(authorizationId ->
                authorizationRepository.updateStatusById(authorizationId, Authorization.Status.COMPLETED));
    }

    @Override
    public void onFailure(UUID tradeId) {
        tradeOutboxRepository.findAuthorizationIdById(tradeId).ifPresent(authorizationId ->
                authorizationRepository.updateStatusById(authorizationId, Authorization.Status.FAILED));
    }
}
