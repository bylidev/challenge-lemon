package me.lemon.challenge.lopez.application.outbound;

import java.util.UUID;

public interface CardValidation {
    boolean isValid(UUID cardId);
}
