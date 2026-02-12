package com.firefly.domain.product.pricing.core.fees.workflows;

import org.fireflyframework.cqrs.command.CommandBus;
import com.firefly.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import org.fireflyframework.transactional.saga.annotations.Saga;
import org.fireflyframework.transactional.saga.annotations.SagaStep;
import org.fireflyframework.transactional.saga.annotations.StepEvent;
import org.fireflyframework.transactional.saga.core.SagaContext;
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
    public Mono<UUID> updateFeeApplicationRule(UpdateFeeApplicationRuleCommand cmd, SagaContext ctx) {
        return commandBus.send(cmd);
    }

}