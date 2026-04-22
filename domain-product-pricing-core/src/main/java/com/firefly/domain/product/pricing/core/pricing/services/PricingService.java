package com.firefly.domain.product.pricing.core.pricing.services;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryResponse;
import com.firefly.domain.product.pricing.core.pricing.simulation.PricingSchemeUpdate;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactReport;
import com.firefly.domain.product.pricing.core.waivers.WaiverSpec;
import com.firefly.domain.product.pricing.core.waivers.WaiverSummary;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingService {


    /**
     * Registers the pricing information for a product.
     *
     * @param command the {@link RegisterProductPricingCommand} containing the pricing details and
     *                product identifier to be registered
     * @return a {@link Mono} emitting a {@link SagaResult} indicating the result of the operation
     */
    Mono<SagaResult> registerPricing(RegisterProductPricingCommand command);

    /**
     * Amends the pricing details for a product by creating a new effective version of the pricing information.
     *
     * @param command the {@link UpdateProductPricingCommand} containing the updated pricing details to be applied
     * @return a {@link Mono} emitting a {@link SagaResult} indicating the result of the pricing amendment operation
     */
    Mono<SagaResult> amendPricing(UpdateProductPricingCommand command);

    /**
     * Project the portfolio-wide impact of a proposed pricing scheme change.
     */
    Mono<SchemeImpactReport> simulateSchemeImpact(UUID schemeId, PricingSchemeUpdate proposed);

    /**
     * Create a pricing waiver on a product.
     */
    Mono<UUID> createWaiver(UUID productId, WaiverSpec spec, UUID tenantId);

    /**
     * Update an existing waiver.
     */
    Mono<UUID> updateWaiver(UUID productId, UUID waiverId, WaiverSpec spec);

    /**
     * Remove a waiver.
     */
    Mono<Void> removeWaiver(UUID productId, UUID waiverId);

    /**
     * Search waivers on a product, optionally filtering by code substring and a reference date.
     */
    Mono<List<WaiverSummary>> searchWaivers(UUID productId, String codeContains, LocalDate activeOn);

    /**
     * Best-effort audit timeline for a pricing entity.
     */
    Mono<PricingHistoryResponse> getPricingHistory(UUID entityId, Optional<LocalDate> from, Optional<LocalDate> to);
}
