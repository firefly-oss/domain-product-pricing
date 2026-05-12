/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.firefly.domain.product.pricing.core.eligibility.services.impl;

import com.firefly.core.customer.sdk.api.PartiesApi;
import com.firefly.core.customer.sdk.model.PartyDTO;
import com.firefly.core.product.sdk.api.ProductPricingApi;
import com.firefly.core.product.sdk.model.ProductPricingDTO;
import com.firefly.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.results.EligibilityResultDTO;
import com.firefly.domain.product.pricing.core.eligibility.services.EligibilityService;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private static final String PARTY_NOT_FOUND = "PARTY_NOT_FOUND";
    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    private static final String PRODUCT_NOT_AVAILABLE = "PRODUCT_NOT_AVAILABLE";
    private static final String AMOUNT_BELOW_MINIMUM = "AMOUNT_BELOW_MINIMUM";
    private static final String AMOUNT_ABOVE_MAXIMUM = "AMOUNT_ABOVE_MAXIMUM";

    private final SagaEngine engine;
    private final PartiesApi partiesApi;
    private final ProductPricingApi productPricingApi;

    @Autowired
    public EligibilityServiceImpl(SagaEngine engine,
                                  @Qualifier("partiesApi") PartiesApi partiesApi,
                                  @Qualifier("productPricingApi") ProductPricingApi productPricingApi) {
        this.engine = engine;
        this.partiesApi = partiesApi;
        this.productPricingApi = productPricingApi;
    }

    @Override
    public Mono<SagaResult> publishEligibility(PublishEligibilityCommand command) {
        return null;
    }

    @Override
    public Mono<SagaResult> adjustEligibility(AdjustEligibilityCommand command) {
        return null;
    }

    @Override
    public Mono<EligibilityResultDTO> evaluateEligibility(EvaluateEligibilityCommand command) {
        String idempotencyKey = UUID.randomUUID().toString();

        Mono<PartyLookup> partyLookup = partiesApi.getPartyById(command.getPartyId(), idempotencyKey)
                .map(party -> new PartyLookup(party, true))
                .onErrorResume(EligibilityServiceImpl::isNotFound,
                        e -> Mono.just(new PartyLookup(null, false)));

        Mono<ProductLookup> productLookup = productPricingApi.getProductPricing(command.getProductId(), idempotencyKey)
                .map(product -> new ProductLookup(product, true))
                .onErrorResume(EligibilityServiceImpl::isNotFound,
                        e -> Mono.just(new ProductLookup(null, false)));

        return Mono.zip(partyLookup, productLookup)
                .map(tuple -> buildResult(command.getRequestedAmount(), tuple.getT1(), tuple.getT2()));
    }

    private static EligibilityResultDTO buildResult(BigDecimal requestedAmount,
                                                    PartyLookup partyLookup,
                                                    ProductLookup productLookup) {
        List<String> reasons = new ArrayList<>();

        if (!partyLookup.found) {
            reasons.add(PARTY_NOT_FOUND);
        }

        BigDecimal maxAmount = null;
        if (!productLookup.found) {
            reasons.add(PRODUCT_NOT_FOUND);
        } else {
            ProductPricingDTO product = productLookup.product;
            maxAmount = product.getMaxAmount();
            if (!Boolean.TRUE.equals(product.getAvailable())) {
                reasons.add(PRODUCT_NOT_AVAILABLE);
            }
            if (product.getMinAmount() != null && requestedAmount.compareTo(product.getMinAmount()) < 0) {
                reasons.add(AMOUNT_BELOW_MINIMUM);
            }
            if (product.getMaxAmount() != null && requestedAmount.compareTo(product.getMaxAmount()) > 0) {
                reasons.add(AMOUNT_ABOVE_MAXIMUM);
            }
        }

        return EligibilityResultDTO.builder()
                .evaluationId(UUID.randomUUID())
                .eligible(reasons.isEmpty())
                .maxAmount(maxAmount)
                .reasons(reasons)
                .build();
    }

    private static boolean isNotFound(Throwable throwable) {
        return throwable instanceof WebClientResponseException ex && ex.getStatusCode().value() == 404;
    }

    private record PartyLookup(PartyDTO party, boolean found) {}

    private record ProductLookup(ProductPricingDTO product, boolean found) {}
}
