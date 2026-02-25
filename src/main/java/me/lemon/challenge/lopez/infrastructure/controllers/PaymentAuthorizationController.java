package me.lemon.challenge.lopez.infrastructure.controllers;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.RequiredArgsConstructor;
import me.lemon.challenge.lopez.application.inbound.PaymentAuthorization;
import me.lemon.challenge.lopez.infrastructure.controllers.dto.PaymentAuthorizationHttpRequest;
import me.lemon.challenge.lopez.infrastructure.facades.DomainIdMapperFacade;

@Controller("/v1/authorizations")
@RequiredArgsConstructor
public class PaymentAuthorizationController {

    private final PaymentAuthorization paymentAuthorization;
    private final DomainIdMapperFacade domainIdMapperFacade;

    @Post
    public void authorize(@Body PaymentAuthorizationHttpRequest request) {
        paymentAuthorization.authorize(domainIdMapperFacade.convertToDomainId(request));
    }

}
