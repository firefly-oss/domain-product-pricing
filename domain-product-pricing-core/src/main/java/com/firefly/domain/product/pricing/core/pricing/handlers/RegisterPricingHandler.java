package com.firefly.domain.product.pricing.core.pricing.handlers;

import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@CommandHandlerComponent
public class RegisterPricingHandler extends CommandHandler<RegisterProductPricingCommand, UUID> {

    private final ProductConfigurationApi productConfigurationApi;

    public RegisterPricingHandler(ProductConfigurationApi productConfigurationApi) {
        this.productConfigurationApi = productConfigurationApi;
    }

    @Override
    protected Mono<UUID> doHandle(RegisterProductPricingCommand cmd) {
        return productConfigurationApi.createConfiguration(cmd.getProductId(), cmd, UUID.randomUUID().toString())
                .mapNotNull(productConfigurationDTO ->
                        Objects.requireNonNull(Objects.requireNonNull(productConfigurationDTO)).getProductConfigurationId());
    }
}
