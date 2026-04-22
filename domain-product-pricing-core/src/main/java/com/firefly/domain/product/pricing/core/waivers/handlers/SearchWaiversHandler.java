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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.model.FilterRequestProductConfigurationDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.domain.product.pricing.core.waivers.WaiverSpec;
import com.firefly.domain.product.pricing.core.waivers.WaiverSummary;
import com.firefly.domain.product.pricing.core.waivers.queries.WaiverSearchQuery;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@QueryHandlerComponent
@Slf4j
public class SearchWaiversHandler extends QueryHandler<WaiverSearchQuery, List<WaiverSummary>> {

    private static final String WAIVER_KEY_PREFIX = "WAIVER_";

    private final ProductConfigurationApi productConfigurationApi;
    private final ObjectMapper objectMapper;

    public SearchWaiversHandler(ProductConfigurationApi productConfigurationApi, ObjectMapper objectMapper) {
        this.productConfigurationApi = productConfigurationApi;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Mono<List<WaiverSummary>> doHandle(WaiverSearchQuery query) {
        return productConfigurationApi.filterConfigurations(query.productId(), new FilterRequestProductConfigurationDTO(), null)
                .map(SearchWaiversHandler::safeContent)
                .defaultIfEmpty(Collections.emptyList())
                .map(raw -> raw.stream()
                        .filter(Objects::nonNull)
                        .map(c -> objectMapper.convertValue(c, ProductConfigurationDTO.class))
                        .filter(SearchWaiversHandler::isWaiver)
                        .filter(cfg -> matchesCode(cfg, query.codeContains()))
                        .map(cfg -> toSummary(cfg, query.productId()))
                        .filter(Objects::nonNull)
                        .filter(summary -> matchesActiveOn(summary, query.activeOn()))
                        .toList());
    }

    private WaiverSummary toSummary(ProductConfigurationDTO cfg, java.util.UUID productId) {
        WaiverSpec spec = deserialize(cfg.getConfigValue(), cfg.getProductConfigurationId());
        if (spec == null) {
            return null;
        }
        return new WaiverSummary(cfg.getProductConfigurationId(), productId, spec);
    }

    private WaiverSpec deserialize(String raw, java.util.UUID waiverId) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            var node = objectMapper.readTree(raw);
            if (node == null || !node.has("spec")) {
                return objectMapper.readValue(raw, WaiverSpec.class);
            }
            return objectMapper.treeToValue(node.get("spec"), WaiverSpec.class);
        } catch (Exception e) {
            log.warn("waivers.deserialize-failed waiverId={} err={}", waiverId, e.getMessage());
            return null;
        }
    }

    private static boolean isWaiver(ProductConfigurationDTO cfg) {
        return cfg.getConfigKey() != null && cfg.getConfigKey().startsWith(WAIVER_KEY_PREFIX);
    }

    private static boolean matchesCode(ProductConfigurationDTO cfg, String codeContains) {
        if (codeContains == null || codeContains.isBlank()) {
            return true;
        }
        return cfg.getConfigKey() != null && cfg.getConfigKey().contains(codeContains);
    }

    private static boolean matchesActiveOn(WaiverSummary summary, LocalDate activeOn) {
        if (activeOn == null) {
            return true;
        }
        WaiverSpec spec = summary.spec();
        if (spec == null) {
            return false;
        }
        if (spec.validFrom() != null && activeOn.isBefore(spec.validFrom())) {
            return false;
        }
        return spec.validTo() == null || !activeOn.isAfter(spec.validTo());
    }

    private static List<Object> safeContent(PaginationResponse resp) {
        if (resp == null || resp.getContent() == null) {
            return Collections.emptyList();
        }
        return resp.getContent();
    }
}
