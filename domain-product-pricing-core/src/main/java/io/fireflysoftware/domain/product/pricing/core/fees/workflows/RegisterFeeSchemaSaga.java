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


package io.fireflysoftware.domain.product.pricing.core.fees.workflows;

import io.fireflysoftware.domain.product.pricing.core.fees.commands.RegisterProductFeeStructureCommand;
import io.fireflysoftware.domain.product.pricing.core.pricing.commands.RemoveProductConfigurationCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.SAGA_REGISTER_FEE_SCHEMA;

/**
 * Optional product_configuration write. Fee structures, components, and rules
 * are not separate aggregates. A missing row does not fail the product.
 */
@Saga(name = SAGA_REGISTER_FEE_SCHEMA)
@Service
public class RegisterFeeSchemaSaga {

    private static final UUID SKIPPED = new UUID(0L, 0L);

    private final CommandBus commandBus;

    @Autowired
    public RegisterFeeSchemaSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = "registerProductFeeStructure", compensate = "removeProductFeeConfiguration")
    @StepEvent(type = "productConfiguration.registered")
    public Mono<UUID> registerProductFeeStructure(RegisterProductFeeStructureCommand cmd, ExecutionContext ctx) {
        if (cmd == null || cmd.getProductId() == null) {
            return Mono.just(SKIPPED);
        }
        ctx.putVariable("productId", cmd.getProductId());
        return commandBus.send(cmd);
    }

    public Mono<Void> removeProductFeeConfiguration(UUID configurationId, ExecutionContext ctx) {
        if (configurationId == null || SKIPPED.equals(configurationId)) {
            return Mono.empty();
        }
        return commandBus.send(new RemoveProductConfigurationCommand(
                ctx.getVariableAs("productId", UUID.class), configurationId));
    }
}
