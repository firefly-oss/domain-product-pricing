package com.firefly.domain.product.pricing.core.fees.handlers;

import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@CommandHandlerComponent
public class UpdateFeeApplicationRuleHandler extends CommandHandler<UpdateFeeApplicationRuleCommand, UUID> {

    private final ProductConfigurationApi productConfigurationApi;

    public UpdateFeeApplicationRuleHandler(ProductConfigurationApi productConfigurationApi) {
        this.productConfigurationApi = productConfigurationApi;
    }

    @Override
    protected Mono<UUID> doHandle(UpdateFeeApplicationRuleCommand cmd) {
        return productConfigurationApi.updateConfiguration(cmd.getProductId(), cmd.getFeeApplicationRuleId(), cmd, UUID.randomUUID().toString())
                .mapNotNull(productConfigurationDTO ->
                        Objects.requireNonNull(Objects.requireNonNull(productConfigurationDTO).getProductConfigurationId()));
    }
}
