package me.lemon.challenge.lopez.application.outbound;

import me.lemon.challenge.lopez.domain.Authorization;

import java.util.UUID;

public interface AuthorizationRepository {
    Authorization save(Authorization authorization);

    void updateStatusById(UUID id, Authorization.Status status);
}
