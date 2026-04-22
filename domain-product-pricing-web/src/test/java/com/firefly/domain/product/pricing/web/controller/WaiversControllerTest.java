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

import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.waivers.WaiverSpec;
import com.firefly.domain.product.pricing.core.waivers.WaiverSummary;
import com.firefly.domain.product.pricing.core.waivers.commands.CreateWaiverCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WaiversControllerTest {

    private PricingService service;
    private WaiversController controller;

    @BeforeEach
    void setUp() {
        service = mock(PricingService.class);
        controller = new WaiversController(service);
    }

    @Test
    void createWaiver_returnsWaiverId() {
        UUID productId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();
        WaiverSpec spec = new WaiverSpec("X", "d", null, BigDecimal.ZERO, null, null);
        when(service.createWaiver(eq(productId), any(WaiverSpec.class), eq(tenantId))).thenReturn(Mono.just(waiverId));

        StepVerifier.create(controller.createWaiver(new CreateWaiverCommand(productId, spec, tenantId)))
                .assertNext(entity -> assertThat(entity.getBody()).isEqualTo(waiverId))
                .verifyComplete();
    }

    @Test
    void updateWaiver_invokesServiceWithPathVars() {
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();
        WaiverSpec spec = new WaiverSpec("Y", null, null, null, null, null);
        when(service.updateWaiver(eq(productId), eq(waiverId), any())).thenReturn(Mono.just(waiverId));

        StepVerifier.create(controller.updateWaiver(waiverId, productId, spec))
                .assertNext(entity -> assertThat(entity.getBody()).isEqualTo(waiverId))
                .verifyComplete();
    }

    @Test
    void removeWaiver_returnsOkWithNoBody() {
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();
        when(service.removeWaiver(eq(productId), eq(waiverId))).thenReturn(Mono.empty());

        StepVerifier.create(controller.removeWaiver(waiverId, productId))
                .assertNext(entity -> assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue())
                .verifyComplete();

        verify(service).removeWaiver(productId, waiverId);
    }

    @Test
    void searchWaivers_returnsList() {
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();
        WaiverSpec spec = new WaiverSpec("Z", null, null, null, null, null);
        List<WaiverSummary> list = List.of(new WaiverSummary(waiverId, productId, spec));
        when(service.searchWaivers(eq(productId), eq("Z"), eq(LocalDate.of(2026, 6, 1))))
                .thenReturn(Mono.just(list));

        StepVerifier.create(controller.searchWaivers(productId, "Z", LocalDate.of(2026, 6, 1)))
                .assertNext(entity -> assertThat(entity.getBody()).isEqualTo(list))
                .verifyComplete();
    }
}
