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
