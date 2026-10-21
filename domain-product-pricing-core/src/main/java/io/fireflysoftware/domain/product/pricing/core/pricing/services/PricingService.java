/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.domain.product.pricing.core.pricing.services;

import io.fireflysoftware.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import io.fireflysoftware.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
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
