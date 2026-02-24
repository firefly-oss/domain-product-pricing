package com.firefly.domain.product.pricing.core.fees.workflows;

import org.fireflyframework.cqrs.command.CommandBus;
import com.firefly.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.firefly.domain.product.pricing.core.utils.constants.ProductPricingConstants.*;


@Saga(name = SAGA_UPDATE_FEE)
@Service
public class UpdateFeeRuleSaga {

    private final CommandBus commandBus;

    @Autowired
    public UpdateFeeRuleSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = STEP_UPDATE_FEE)
    @StepEvent(type = EVENT_FEE_UPDATED)
    public Mono<UUID> updateFeeApplicationRule(UpdateFeeApplicationRuleCommand cmd, ExecutionContext ctx) {
        return commandBus.send(cmd);
    }

}