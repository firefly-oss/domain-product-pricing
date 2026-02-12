package com.firefly.domain.product.pricing.core.pricing.services;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import org.fireflyframework.transactional.saga.core.SagaResult;
import reactor.core.publisher.Mono;

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

}
