package com.firefly.domain.product.pricing.core.pricing.commands;

import org.fireflyframework.cqrs.command.Command;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateProductPricingCommand extends ProductConfigurationDTO implements Command<UUID> {
    private UUID productPricingId;

    public UpdateProductPricingCommand withProductPricingId(UUID productPricingId){
        this.setProductPricingId(productPricingId);
        return this;
    }
}
