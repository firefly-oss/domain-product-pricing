package io.fireflysoftware.domain.product.pricing.core.pricing.workflows;

import io.fireflysoftware.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
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

import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.EVENT_PRODUCT_PRICING_REGISTERED;
import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.SAGA_REGISTER_PRICING;
import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.STEP_REGISTER_PRODUCT_PRICING;

/**
 * Stores optional pricing configuration. A product with no amount, rate, or fee
 * rows is still valid, so this step is not required to create the product.
 */
@Saga(name = SAGA_REGISTER_PRICING)
@Service
public class RegisterPricingSaga {

    private static final UUID SKIPPED = new UUID(0L, 0L);

    private final CommandBus commandBus;

    @Autowired
    public RegisterPricingSaga(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @SagaStep(id = STEP_REGISTER_PRODUCT_PRICING, compensate = "removePricingConfiguration")
    @StepEvent(type = EVENT_PRODUCT_PRICING_REGISTERED)
    public Mono<UUID> registerPricing(RegisterProductPricingCommand cmd, ExecutionContext ctx) {
        if (cmd == null || cmd.getProductId() == null) {
            return Mono.just(SKIPPED);
        }
        ctx.putVariable("productId", cmd.getProductId());
        return commandBus.send(cmd);
    }

    public Mono<Void> removePricingConfiguration(UUID configurationId, ExecutionContext ctx) {
        if (configurationId == null || SKIPPED.equals(configurationId)) {
            return Mono.empty();
        }
        UUID productId = ctx.getVariableAs("productId", UUID.class);
        return commandBus.send(new RemoveProductConfigurationCommand(productId, configurationId));
    }
}
