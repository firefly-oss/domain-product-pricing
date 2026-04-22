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

package com.firefly.domain.product.pricing.core.pricing.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.api.ProductVersionApi;
import com.firefly.core.product.sdk.model.FilterRequestProductVersionDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductVersionDTO;
import com.firefly.domain.product.pricing.core.pricing.history.impl.PricingHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingHistoryServiceImplTest {

    private ProductVersionApi productVersionApi;
    private ProductConfigurationApi productConfigurationApi;
    private PricingHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        productVersionApi = mock(ProductVersionApi.class);
        productConfigurationApi = mock(ProductConfigurationApi.class);
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        service = new PricingHistoryServiceImpl(productConfigurationApi, productVersionApi, objectMapper);
    }

    @Test
    void getHistory_noVersions_returnsSingleSyntheticEntry() {
        UUID entityId = UUID.randomUUID();
        when(productVersionApi.filterProductVersions(eq(entityId), any(FilterRequestProductVersionDTO.class), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of())));

        StepVerifier.create(service.getHistory(entityId, Optional.empty(), Optional.empty()))
                .assertNext(resp -> {
                    assertThat(resp.entityId()).isEqualTo(entityId);
                    assertThat(resp.entries()).hasSize(1);
                    assertThat(resp.entries().get(0).actor()).isEqualTo("system");
                    assertThat(resp.entries().get(0).action()).isEqualTo("CREATED");
                })
                .verifyComplete();
    }

    @Test
    void getHistory_versionsExist_returnsVersionedEntries() {
        UUID entityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProductVersionDTO v1 = new ProductVersionDTO(now, now, UUID.randomUUID())
                .versionNumber(1L).versionDescription("v1").effectiveDate(now);
        ProductVersionDTO v2 = new ProductVersionDTO(now, now, UUID.randomUUID())
                .versionNumber(2L).versionDescription("v2").effectiveDate(now);

        when(productVersionApi.filterProductVersions(eq(entityId), any(), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of(v1, v2))));

        StepVerifier.create(service.getHistory(entityId, Optional.empty(), Optional.empty()))
                .assertNext(resp -> {
                    assertThat(resp.entries()).hasSize(2);
                    assertThat(resp.entries()).allMatch(e -> "VERSIONED".equals(e.action()));
                })
                .verifyComplete();
    }

    @Test
    void getHistory_swallowsErrorsAndReturnsSynthetic() {
        UUID entityId = UUID.randomUUID();
        when(productVersionApi.filterProductVersions(eq(entityId), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(service.getHistory(entityId, Optional.empty(), Optional.empty()))
                .assertNext(resp -> {
                    assertThat(resp.entries()).hasSize(1);
                    assertThat(resp.entries().get(0).actor()).isEqualTo("system");
                })
                .verifyComplete();
    }
}
