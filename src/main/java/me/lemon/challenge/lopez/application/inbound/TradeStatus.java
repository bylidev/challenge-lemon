package me.lemon.challenge.lopez.application.inbound;

import java.util.UUID;

public interface TradeStatus {
    void onCompleted(UUID tradeId);

    void onFailure(UUID tradeId);
}
