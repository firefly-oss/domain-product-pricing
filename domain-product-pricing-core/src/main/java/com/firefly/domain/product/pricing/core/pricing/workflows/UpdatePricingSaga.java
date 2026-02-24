package com.firefly.domain.product.pricing.core.pricing.workflows;

import org.fireflyframework.cqrs.command.CommandBus;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.firefly.domain.product.pricing.core.utils.constants.ProductPricingConstants.*;


@Saga(name = SAGA_UPDATE_PRICING)
@Service
public class UpdatePricingSaga {

    private final CommandBus commandBus;

    @Autowired
    public UpdatePricingSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = STEP_UPDATE_PRICING)
    @StepEvent(type = EVENT_PRICING_UPDATED)
    public Mono<UUID> updatePricing(UpdateProductPricingCommand cmd, ExecutionContext ctx) {
        return commandBus.send(cmd);
    }

}