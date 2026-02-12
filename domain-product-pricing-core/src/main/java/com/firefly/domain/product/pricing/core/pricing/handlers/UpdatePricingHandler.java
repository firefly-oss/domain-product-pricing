package com.firefly.domain.product.pricing.core.pricing.handlers;

import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@CommandHandlerComponent
public class UpdatePricingHandler extends CommandHandler<UpdateProductPricingCommand, UUID> {

    private final ProductConfigurationApi productConfigurationApi;

    public UpdatePricingHandler(ProductConfigurationApi productConfigurationApi) {
        this.productConfigurationApi = productConfigurationApi;
    }

    @Override
    protected Mono<UUID> doHandle(UpdateProductPricingCommand cmd) {
        return productConfigurationApi.updateConfiguration(cmd.getProductId(), cmd.getProductPricingId(), cmd, UUID.randomUUID().toString())
                .mapNotNull(productConfigurationDTO ->
                        Objects.requireNonNull(Objects.requireNonNull(productConfigurationDTO).getProductConfigurationId()));
    }
}
