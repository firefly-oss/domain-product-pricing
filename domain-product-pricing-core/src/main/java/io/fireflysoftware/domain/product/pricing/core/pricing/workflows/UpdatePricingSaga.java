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


package io.fireflysoftware.domain.product.pricing.core.pricing.workflows;

import io.fireflysoftware.core.product.sdk.api.ProductConfigurationApi;
import io.fireflysoftware.core.product.sdk.model.ProductConfigurationDTO;
import io.fireflysoftware.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.EVENT_PRICING_UPDATED;
import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.SAGA_UPDATE_PRICING;
import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.STEP_UPDATE_PRICING;

@Saga(name = SAGA_UPDATE_PRICING)
@Service
public class UpdatePricingSaga {

    private final CommandBus commandBus;
    private final ProductConfigurationApi productConfigurationApi;

    @Autowired
    public UpdatePricingSaga(CommandBus commandBus, ProductConfigurationApi productConfigurationApi) {
        this.commandBus = commandBus;
        this.productConfigurationApi = productConfigurationApi;
    }

    @SagaStep(id = "readPricing")
    @StepEvent(type = "pricing.read")
    public Mono<ProductConfigurationDTO> readPricing(UpdateProductPricingCommand cmd, ExecutionContext ctx) {
        return productConfigurationApi.getConfigurationById(
                        cmd.getProductId(), cmd.getProductPricingId(), UUID.randomUUID().toString())
                .doOnNext(previous -> ctx.putVariable("previousPricing", previous));
    }

    @SagaStep(id = STEP_UPDATE_PRICING, dependsOn = "readPricing", compensate = "restorePricing")
    @StepEvent(type = EVENT_PRICING_UPDATED)
    public Mono<UUID> updatePricing(UpdateProductPricingCommand cmd, ExecutionContext ctx) {
        return commandBus.send(cmd);
    }

    public Mono<Void> restorePricing(UUID configurationId, ExecutionContext ctx) {
        ProductConfigurationDTO previous = ctx.getVariableAs("previousPricing", ProductConfigurationDTO.class);
        if (previous == null) {
            return Mono.empty();
        }
        UpdateProductPricingCommand restore = new UpdateProductPricingCommand();
        BeanUtils.copyProperties(previous, restore);
        return commandBus.send(restore).then();
    }
}
