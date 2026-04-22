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

package com.firefly.domain.product.pricing.web.controller;

import com.firefly.domain.product.pricing.core.pricing.history.HistoryEntry;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryResponse;
import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.pricing.simulation.PricingSchemeUpdate;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactReport;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactStats;
import com.firefly.domain.product.pricing.core.pricing.simulation.SimulateSchemeImpactRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingControllerTest {

    private PricingService service;
    private PricingController controller;

    @BeforeEach
    void setUp() {
        service = mock(PricingService.class);
        controller = new PricingController(service);
    }

    @Test
    void simulateSchemeImpact_forwardsToService() {
        UUID schemeId = UUID.randomUUID();
        PricingSchemeUpdate update = new PricingSchemeUpdate(new BigDecimal("0.06"), null, null);
        SchemeImpactReport report = new SchemeImpactReport(schemeId, 0, List.of(),
                new SchemeImpactStats(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(service.simulateSchemeImpact(eq(schemeId), any(PricingSchemeUpdate.class))).thenReturn(Mono.just(report));

        StepVerifier.create(controller.simulateSchemeImpact(schemeId, new SimulateSchemeImpactRequest(update)))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(entity.getBody()).isEqualTo(report);
                })
                .verifyComplete();
    }

    @Test
    void getPricingHistory_forwardsOptionalRangeToService() {
        UUID entityId = UUID.randomUUID();
        PricingHistoryResponse resp = new PricingHistoryResponse(entityId, "PRICING_CONFIG",
                List.of(new HistoryEntry(LocalDateTime.now(), "CREATED", "system", "")));
        when(service.getPricingHistory(eq(entityId),
                eq(Optional.of(LocalDate.of(2026, 1, 1))),
                eq(Optional.of(LocalDate.of(2026, 12, 31)))))
                .thenReturn(Mono.just(resp));

        StepVerifier.create(controller.getPricingHistory(entityId,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .assertNext(entity -> assertThat(entity.getBody()).isEqualTo(resp))
                .verifyComplete();
    }

    @Test
    void getPricingHistory_nullRangeMapsToEmptyOptionals() {
        UUID entityId = UUID.randomUUID();
        PricingHistoryResponse resp = new PricingHistoryResponse(entityId, "PRICING_CONFIG", List.of());
        when(service.getPricingHistory(eq(entityId), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(Mono.just(resp));

        StepVerifier.create(controller.getPricingHistory(entityId, null, null))
                .assertNext(entity -> assertThat(entity.getBody()).isEqualTo(resp))
                .verifyComplete();
    }
}
