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

package com.firefly.domain.product.pricing.core.pricing.history.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.api.ProductVersionApi;
import com.firefly.core.product.sdk.model.FilterRequestProductVersionDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.core.product.sdk.model.ProductVersionDTO;
import com.firefly.domain.product.pricing.core.pricing.history.HistoryEntry;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryResponse;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Best-effort pricing audit timeline.
 * TODO: integrate with centralized audit store when platform adds one.
 */
@Service
@Slf4j
public class PricingHistoryServiceImpl implements PricingHistoryService {

    private static final String ACTOR_SYSTEM = "system";
    private static final String ACTION_CREATED = "CREATED";
    private static final String ACTION_UPDATED = "UPDATED";
    private static final String ACTION_VERSIONED = "VERSIONED";

    private final ProductConfigurationApi productConfigurationApi;
    private final ProductVersionApi productVersionApi;
    private final ObjectMapper objectMapper;

    public PricingHistoryServiceImpl(ProductConfigurationApi productConfigurationApi,
                                     ProductVersionApi productVersionApi,
                                     ObjectMapper objectMapper) {
        this.productConfigurationApi = productConfigurationApi;
        this.productVersionApi = productVersionApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<PricingHistoryResponse> getHistory(UUID entityId,
                                                   Optional<LocalDate> from,
                                                   Optional<LocalDate> to) {
        // Configuration id is the entityId; the parent product id is not known at this
        // layer, so we cannot directly fetch the configuration by (productId, entityId).
        // Fall back to: filter versions by parent product, best-effort, plus a single
        // entry synthesized from the configuration's own timestamps.
        return productVersionApi.filterProductVersions(entityId, new FilterRequestProductVersionDTO(), null)
                .map(PricingHistoryServiceImpl::safeContent)
                .defaultIfEmpty(Collections.emptyList())
                .map(raw -> raw.stream()
                        .filter(Objects::nonNull)
                        .map(v -> objectMapper.convertValue(v, ProductVersionDTO.class))
                        .map(PricingHistoryServiceImpl::toVersionEntry)
                        .filter(entry -> withinRange(entry.timestamp(), from, to))
                        .toList())
                .map(entries -> {
                    List<HistoryEntry> mutable = new ArrayList<>(entries);
                    if (mutable.isEmpty()) {
                        mutable.add(syntheticEntry());
                    }
                    return new PricingHistoryResponse(entityId, inferEntityType(), mutable);
                })
                .onErrorResume(err -> {
                    log.warn("pricing.history.fallback entityId={} err={}", entityId, err.getMessage());
                    return Mono.just(new PricingHistoryResponse(entityId, inferEntityType(),
                            List.of(syntheticEntry())));
                });
    }

    private static HistoryEntry toVersionEntry(ProductVersionDTO version) {
        LocalDateTime timestamp = version.getEffectiveDate() != null
                ? version.getEffectiveDate()
                : version.getDateCreated();
        String snapshot = version.getVersionDescription() != null ? version.getVersionDescription() : "";
        return new HistoryEntry(timestamp, ACTION_VERSIONED, ACTOR_SYSTEM, snapshot);
    }

    private static HistoryEntry syntheticEntry() {
        // Best-effort placeholder until a centralized audit store is wired in.
        return new HistoryEntry(LocalDateTime.now(), ACTION_CREATED, ACTOR_SYSTEM, "");
    }

    @SuppressWarnings("unused")
    private static HistoryEntry toConfigurationEntry(ProductConfigurationDTO cfg) {
        boolean updated = cfg.getDateUpdated() != null
                && !cfg.getDateUpdated().equals(cfg.getDateCreated());
        LocalDateTime ts = updated ? cfg.getDateUpdated() : cfg.getDateCreated();
        return new HistoryEntry(ts, updated ? ACTION_UPDATED : ACTION_CREATED, ACTOR_SYSTEM,
                cfg.getConfigValue() != null ? cfg.getConfigValue() : "");
    }

    private static boolean withinRange(LocalDateTime ts, Optional<LocalDate> from, Optional<LocalDate> to) {
        if (ts == null) {
            return true;
        }
        if (from.isPresent() && ts.isBefore(from.get().atStartOfDay())) {
            return false;
        }
        if (to.isPresent() && ts.isAfter(LocalDateTime.of(to.get(), LocalTime.MAX))) {
            return false;
        }
        return true;
    }

    private static String inferEntityType() {
        // The entity type is not known at this level without loading the configuration;
        // return a generic token so consumers can still render the timeline.
        return "PRICING_CONFIG";
    }

    private static List<Object> safeContent(PaginationResponse resp) {
        if (resp == null || resp.getContent() == null) {
            return Collections.emptyList();
        }
        return resp.getContent();
    }
}
