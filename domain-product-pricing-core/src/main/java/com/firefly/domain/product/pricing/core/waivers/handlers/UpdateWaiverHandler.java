/*
 * Copyright 2025 Firefly Software Solutions Inc
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

package com.firefly.domain.product.pricing.core.waivers.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.domain.product.pricing.core.waivers.commands.UpdateWaiverCommand;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.firefly.domain.product.pricing.core.utils.constants.ProductPricingConstants.CONFIG_TYPE_PRICING_WAIVER;

@CommandHandlerComponent
public class UpdateWaiverHandler extends CommandHandler<UpdateWaiverCommand, UUID> {

    private static final String WAIVER_KEY_PREFIX = "WAIVER_";

    private final ProductConfigurationApi productConfigurationApi;
    private final ObjectMapper objectMapper;

    public UpdateWaiverHandler(ProductConfigurationApi productConfigurationApi, ObjectMapper objectMapper) {
        this.productConfigurationApi = productConfigurationApi;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Mono<UUID> doHandle(UpdateWaiverCommand cmd) {
        ProductConfigurationDTO dto = new ProductConfigurationDTO()
                .productId(cmd.productId())
                .configType(ProductConfigurationDTO.ConfigTypeEnum.CUSTOM)
                .configKey(WAIVER_KEY_PREFIX + cmd.spec().code())
                .configValue(serialize(cmd));
        return productConfigurationApi.updateConfiguration(cmd.productId(), cmd.waiverId(), dto, UUID.randomUUID().toString())
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "Waiver update returned empty response for waiverId=" + cmd.waiverId())))
                .map(resp -> {
                    UUID id = resp.getProductConfigurationId();
                    return id != null ? id : cmd.waiverId();
                });
    }

    private String serialize(UpdateWaiverCommand cmd) {
        try {
            return objectMapper.writeValueAsString(new WaiverPayload(cmd.spec(), CONFIG_TYPE_PRICING_WAIVER));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize waiver payload", e);
        }
    }

    private record WaiverPayload(Object spec, String configType) {}
}
