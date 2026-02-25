package me.lemon.challenge.lopez.infrastructure.adapters;

import me.lemon.challenge.lopez.application.outbound.CardValidation;

import java.util.UUID;

/**
 * Note: Suppress warnings for this mock.
 * Used for demonstration purposes only.
 */
public class MockedCardValidationAdapter implements CardValidation {
    @Override
    public boolean isValid(UUID cardId) {
        return true;
    }
}
