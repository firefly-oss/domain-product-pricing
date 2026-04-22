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

package com.firefly.domain.product.pricing.core.pricing.simulation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.model.FilterRequestProductConfigurationDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.domain.product.pricing.core.pricing.simulation.PricingSchemeUpdate;
import com.firefly.domain.product.pricing.core.pricing.simulation.ProductImpact;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactReport;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactStats;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactSimulationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Impact simulator for pricing scheme changes. Loads pricing configurations
 * that reference the given scheme, projects current vs proposed monthly
 * payments under a reference scenario, and reports per-product deltas plus
 * aggregate statistics.
 */
@Service
@Slf4j
public class SchemeImpactSimulationServiceImpl implements SchemeImpactSimulationService {

    private static final MathContext MC = new MathContext(12);
    private static final int SCALE_MONEY = 2;
    private static final int SCALE_PCT = 4;
    private static final BigDecimal REFERENCE_AMOUNT = new BigDecimal("10000");
    private static final int REFERENCE_TENOR_MONTHS = 12;
    private static final int MAX_CONCURRENCY = 8;

    private final ProductConfigurationApi productConfigurationApi;
    private final ObjectMapper objectMapper;

    public SchemeImpactSimulationServiceImpl(ProductConfigurationApi productConfigurationApi,
                                             ObjectMapper objectMapper) {
        this.productConfigurationApi = productConfigurationApi;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<SchemeImpactReport> simulate(UUID schemeId, PricingSchemeUpdate proposed) {
        return productConfigurationApi.filterConfigurations(schemeId, new FilterRequestProductConfigurationDTO(), null)
                .map(SchemeImpactSimulationServiceImpl::safeContent)
                .defaultIfEmpty(Collections.emptyList())
                .flatMap(raw -> {
                    List<ProductConfigurationDTO> configs = raw.stream()
                            .filter(Objects::nonNull)
                            .map(c -> objectMapper.convertValue(c, ProductConfigurationDTO.class))
                            .filter(cfg -> cfg.getProductId() != null)
                            .toList();

                    List<ProductConfigurationDTO> relevant = configs.stream()
                            .filter(cfg -> referencesScheme(cfg, schemeId))
                            .toList();

                    return Flux.fromIterable(relevant)
                            .flatMap(cfg -> Mono.fromSupplier(() -> buildImpact(cfg, proposed)), MAX_CONCURRENCY)
                            .collectList()
                            .map(impacts -> new SchemeImpactReport(
                                    schemeId,
                                    impacts.size(),
                                    impacts,
                                    computeStats(impacts)));
                });
    }

    // ============================== IMPACT CALCULATION ==============================

    private ProductImpact buildImpact(ProductConfigurationDTO cfg, PricingSchemeUpdate proposed) {
        JsonNode current = parseOrEmpty(cfg.getConfigValue(), cfg.getConfigKey());
        BigDecimal currentRate = readDecimal(current, "interestRate");
        BigDecimal newRate = proposed.newInterestRate() != null ? proposed.newInterestRate() : currentRate;

        BigDecimal currentPayment = amortize(REFERENCE_AMOUNT, currentRate, REFERENCE_TENOR_MONTHS);
        BigDecimal newPayment = amortize(REFERENCE_AMOUNT, newRate, REFERENCE_TENOR_MONTHS);
        BigDecimal delta = deltaPercent(currentPayment, newPayment);

        return new ProductImpact(cfg.getProductId(), currentPayment, newPayment, delta);
    }

    private static BigDecimal amortize(BigDecimal principal, BigDecimal annualRate, int tenor) {
        if (annualRate == null || annualRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenor), SCALE_MONEY, RoundingMode.HALF_UP);
        }
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), MC);
        BigDecimal factor = BigDecimal.ONE.add(monthlyRate, MC).pow(tenor, MC);
        BigDecimal denom = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, MC), MC);
        if (denom.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenor), SCALE_MONEY, RoundingMode.HALF_UP);
        }
        return principal.multiply(monthlyRate, MC).divide(denom, MC).setScale(SCALE_MONEY, RoundingMode.HALF_UP);
    }

    private static BigDecimal deltaPercent(BigDecimal current, BigDecimal proposed) {
        if (current == null || current.signum() == 0) {
            return BigDecimal.ZERO.setScale(SCALE_PCT, RoundingMode.HALF_UP);
        }
        return proposed.subtract(current, MC).divide(current, MC).setScale(SCALE_PCT, RoundingMode.HALF_UP);
    }

    // ============================== STATS ==============================

    private static SchemeImpactStats computeStats(List<ProductImpact> impacts) {
        if (impacts.isEmpty()) {
            return new SchemeImpactStats(
                    BigDecimal.ZERO.setScale(SCALE_PCT, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(SCALE_PCT, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(SCALE_PCT, RoundingMode.HALF_UP));
        }
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;
        for (ProductImpact impact : impacts) {
            BigDecimal d = impact.deltaPercent() != null ? impact.deltaPercent() : BigDecimal.ZERO;
            sum = sum.add(d, MC);
            if (max == null || d.compareTo(max) > 0) {
                max = d;
            }
            if (min == null || d.compareTo(min) < 0) {
                min = d;
            }
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(impacts.size()), SCALE_PCT, RoundingMode.HALF_UP);
        return new SchemeImpactStats(avg,
                max != null ? max : BigDecimal.ZERO,
                min != null ? min : BigDecimal.ZERO);
    }

    // ============================== HELPERS ==============================

    private boolean referencesScheme(ProductConfigurationDTO cfg, UUID schemeId) {
        if (cfg == null || schemeId == null) {
            return false;
        }
        if (!isPricingScheme(cfg)) {
            return false;
        }
        JsonNode parsed = parseOrEmpty(cfg.getConfigValue(), cfg.getConfigKey());
        String ref = parsed != null && parsed.hasNonNull("schemeId") ? parsed.get("schemeId").asText() : null;
        if (ref != null) {
            return schemeId.toString().equals(ref);
        }
        // Fall back: treat every pricing config on the caller's productId as affected.
        return schemeId.equals(cfg.getProductId());
    }

    private static boolean isPricingScheme(ProductConfigurationDTO cfg) {
        return cfg.getConfigType() != null
                && cfg.getConfigType().getValue() != null
                && cfg.getConfigType().getValue().contains("PRICING");
    }

    private JsonNode parseOrEmpty(String raw, String key) {
        if (raw == null || raw.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            log.warn("scheme-impact.config-parse-failed key={} err={}", key, e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private static BigDecimal readDecimal(JsonNode node, String field) {
        if (node != null && node.hasNonNull(field)) {
            return node.get(field).decimalValue();
        }
        return BigDecimal.ZERO;
    }

    private static List<Object> safeContent(PaginationResponse resp) {
        if (resp == null || resp.getContent() == null) {
            return new ArrayList<>();
        }
        return resp.getContent();
    }
}
