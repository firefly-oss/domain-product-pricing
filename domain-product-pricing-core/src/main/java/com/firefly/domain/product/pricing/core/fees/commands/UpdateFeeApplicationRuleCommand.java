package com.firefly.domain.product.pricing.core.fees.commands;

import org.fireflyframework.cqrs.command.Command;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateFeeApplicationRuleCommand extends ProductConfigurationDTO implements Command<UUID> {
    private UUID feeApplicationRuleId;
    private UUID feeComponentId;

    public UpdateFeeApplicationRuleCommand withFeeApplicationRuleId(UUID feeApplicationRuleId){
        this.setFeeApplicationRuleId(feeApplicationRuleId);
        return this;
    }

    public UpdateFeeApplicationRuleCommand withFeeComponentId(UUID componentId){
        this.setFeeComponentId(componentId);
        return this;
    }
}
