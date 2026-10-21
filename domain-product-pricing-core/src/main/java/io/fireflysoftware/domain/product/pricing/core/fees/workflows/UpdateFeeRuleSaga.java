/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.domain.product.pricing.core.fees.workflows;

import io.fireflysoftware.core.product.sdk.api.ProductConfigurationApi;
import io.fireflysoftware.core.product.sdk.model.ProductConfigurationDTO;
import io.fireflysoftware.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.EVENT_FEE_UPDATED;
import static io.fireflysoftware.domain.product.pricing.core.utils.constants.ProductPricingConstants.STEP_UPDATE_FEE;

/**
 * Updates one product configuration row that used to be called a fee rule,
 * and restores the previous row if a later step fails.
 */
@Saga(name = "UpdateFeeRuleSaga")
@Service
public class UpdateFeeRuleSaga {

    private final CommandBus commandBus;
    private final ProductConfigurationApi productConfigurationApi;

    @Autowired
    public UpdateFeeRuleSaga(CommandBus commandBus, ProductConfigurationApi productConfigurationApi) {
        this.commandBus = commandBus;
        this.productConfigurationApi = productConfigurationApi;
    }

    @SagaStep(id = "readFeeRule")
    @StepEvent(type = "feeRule.read")
    public Mono<ProductConfigurationDTO> readFeeRule(UpdateFeeApplicationRuleCommand cmd, ExecutionContext ctx) {
        return productConfigurationApi.getConfigurationById(
                        cmd.getProductId(), cmd.getFeeApplicationRuleId(), UUID.randomUUID().toString())
                .doOnNext(previous -> ctx.putVariable("previousFeeRule", previous));
    }

    @SagaStep(id = STEP_UPDATE_FEE, dependsOn = "readFeeRule", compensate = "restoreFeeRule")
    @StepEvent(type = EVENT_FEE_UPDATED)
    public Mono<UUID> updateFeeApplicationRule(UpdateFeeApplicationRuleCommand cmd, ExecutionContext ctx) {
        return commandBus.send(cmd);
    }

    public Mono<Void> restoreFeeRule(UUID configurationId, ExecutionContext ctx) {
        ProductConfigurationDTO previous = ctx.getVariableAs("previousFeeRule", ProductConfigurationDTO.class);
        if (previous == null) {
            return Mono.empty();
        }
        UpdateFeeApplicationRuleCommand restore = new UpdateFeeApplicationRuleCommand();
        BeanUtils.copyProperties(previous, restore);
        restore.setProductId(previous.getProductId());
        UUID ruleId = previous.getProductConfigurationId() != null
                ? previous.getProductConfigurationId()
                : configurationId;
        restore.setFeeApplicationRuleId(ruleId);
        return commandBus.send(restore).then();
    }
}
