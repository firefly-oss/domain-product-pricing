package com.firefly.domain.product.pricing.core.fees.workflows;

import org.fireflyframework.cqrs.command.CommandBus;
import com.firefly.domain.product.pricing.core.fees.commands.*;
import org.fireflyframework.transactional.saga.annotations.Saga;
import org.fireflyframework.transactional.saga.annotations.SagaStep;
import org.fireflyframework.transactional.saga.annotations.StepEvent;
import org.fireflyframework.transactional.saga.core.SagaContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.firefly.domain.product.pricing.core.utils.constants.GlobalConstants.*;
import static com.firefly.domain.product.pricing.core.utils.constants.ProductPricingConstants.*;


@Saga(name = SAGA_REGISTER_FEE_SCHEMA)
@Service
public class RegisterFeeSchemaSaga {

    private final CommandBus commandBus;

    @Autowired
    public RegisterFeeSchemaSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }


    @SagaStep(id = STEP_REGISTER_FEE_STRUCTURE, compensate = COMPENSATE_REMOVE_FEE_STRUCTURE)
    @StepEvent(type = EVENT_FEE_STRUCTURE_REGISTERED)
    public Mono<UUID> registerFeeStructure(RegisterFeeStructureCommand cmd, SagaContext ctx) {
        return commandBus.send(cmd)
                .doOnNext(freeStructureId -> ctx.variables().put(CTX_FEE_STRUCTURE_ID, freeStructureId));
    }

    public Mono<Void> removeFeeStructure(UUID feeStructureId, SagaContext ctx) {
        return commandBus.send(new RemoveFeeStructureCommand(ctx.getVariableAs(CTX_PRODUCT_ID, UUID.class), feeStructureId));
    }


    @SagaStep(id = STEP_REGISTER_FEE_COMPONENT, compensate = COMPENSATE_REMOVE_FEE_COMPONENT, dependsOn = STEP_REGISTER_FEE_STRUCTURE)
    @StepEvent(type = EVENT_FEE_COMPONENT_REGISTERED)
    public Mono<UUID> registerFeeComponent(RegisterFeeComponentCommand cmd, SagaContext ctx) {
        return commandBus.send(cmd.withFeeStructureId(ctx.getVariableAs(CTX_FEE_STRUCTURE_ID, UUID.class)))
                .doOnNext(feeComponentId -> ctx.variables().put(CTX_FEE_COMPONENT_ID, feeComponentId));
    }

    public Mono<Void> removeFeeComponent(UUID feeComponentId, SagaContext ctx) {
        return commandBus.send(new RemoveFeeComponentCommand(ctx.getVariableAs(CTX_PRODUCT_ID, UUID.class), feeComponentId, ctx.getVariableAs(CTX_FEE_STRUCTURE_ID, UUID.class)));
    }

    @SagaStep(id = STEP_REGISTER_FEE_APPLICATION_RULE, compensate = COMPENSATE_REMOVE_FEE_APPLICATION_RULE, dependsOn = {STEP_REGISTER_FEE_COMPONENT, STEP_REGISTER_FEE_STRUCTURE})
    @StepEvent(type = EVENT_FEE_APPLICATION_RULE_REGISTERED)
    public Mono<UUID> registerFeeApplicationRule(RegisterFeeApplicationRuleCommand cmd, SagaContext ctx) {
        return commandBus.send(cmd
                .withFeeComponentId(ctx.getVariableAs(CTX_FEE_COMPONENT_ID, UUID.class)));
    }

    public Mono<Void> removeFeeApplicationRule(UUID feeApplicationRuleId, SagaContext ctx) {
        return commandBus.send(new RemoveFeeApplicationRuleCommand(
                ctx.getVariableAs(CTX_PRODUCT_ID, UUID.class),
                feeApplicationRuleId,
                ctx.getVariableAs(CTX_FEE_STRUCTURE_ID, UUID.class),
                ctx.getVariableAs(CTX_FEE_COMPONENT_ID, UUID.class)));
    }

    @SagaStep(id = STEP_REGISTER_PRODUCT_FEE_STRUCTURE, compensate = COMPENSATE_REMOVE_PRODUCT_FEE_STRUCTURE, dependsOn = {STEP_REGISTER_FEE_STRUCTURE})
    @StepEvent(type = EVENT_PRODUCT_FEE_STRUCTURE_REGISTERED)
    public Mono<UUID> registerProductFeeStructure(RegisterProductFeeStructureCommand cmd, SagaContext ctx) {
        return commandBus.send(cmd
                        .withFeeStructureId(ctx.getVariableAs(CTX_FEE_STRUCTURE_ID, UUID.class)))
                .doOnNext(productFeeStructureId -> ctx.variables().put(CTX_PRODUCT_FEE_STRUCTURE_ID, productFeeStructureId));
    }

    public Mono<Void> removeProductFeeStructure(UUID productFeeStructureId, SagaContext ctx) {
        return commandBus.send(new RemoveProductFeeStructureCommand(ctx.getVariableAs(CTX_PRODUCT_ID, UUID.class), productFeeStructureId));
    }

}
