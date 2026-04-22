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

package com.firefly.domain.product.pricing.core.waivers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.model.FilterRequestProductConfigurationDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.domain.product.pricing.core.waivers.commands.CreateWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.commands.RemoveWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.commands.UpdateWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.handlers.CreateWaiverHandler;
import com.firefly.domain.product.pricing.core.waivers.handlers.RemoveWaiverHandler;
import com.firefly.domain.product.pricing.core.waivers.handlers.SearchWaiversHandler;
import com.firefly.domain.product.pricing.core.waivers.handlers.UpdateWaiverHandler;
import com.firefly.domain.product.pricing.core.waivers.queries.WaiverSearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WaiverHandlersTest {

    private ProductConfigurationApi api;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        api = mock(ProductConfigurationApi.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createWaiverHandler_callsCreateConfigurationWithPrefixedKey() {
        CreateWaiverHandler handler = new CreateWaiverHandler(api, objectMapper);
        UUID productId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        WaiverSpec spec = new WaiverSpec("PROMO10", "desc", null,
                new BigDecimal("-0.01"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        ArgumentCaptor<ProductConfigurationDTO> bodyCaptor = ArgumentCaptor.forClass(ProductConfigurationDTO.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(api.createConfiguration(eq(productId), bodyCaptor.capture(), keyCaptor.capture()))
                .thenReturn(Mono.just(new ProductConfigurationDTO(LocalDateTime.now(), LocalDateTime.now(), createdId)));

        StepVerifier.create(handler.handle(new CreateWaiverCommand(productId, spec, UUID.randomUUID())))
                .expectNext(createdId)
                .verifyComplete();

        assertThat(bodyCaptor.getValue().getConfigKey()).isEqualTo("WAIVER_PROMO10");
        assertThat(bodyCaptor.getValue().getConfigValue()).contains("PROMO10");
        assertUuid(keyCaptor.getValue());
    }

    @Test
    void updateWaiverHandler_invokesUpdateConfiguration() {
        UpdateWaiverHandler handler = new UpdateWaiverHandler(api, objectMapper);
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();
        WaiverSpec spec = new WaiverSpec("NEWCODE", "d", null, new BigDecimal("-0.005"), null, null);

        when(api.updateConfiguration(eq(productId), eq(waiverId), any(), any()))
                .thenReturn(Mono.just(new ProductConfigurationDTO(LocalDateTime.now(), LocalDateTime.now(), waiverId)));

        StepVerifier.create(handler.handle(new UpdateWaiverCommand(productId, waiverId, spec)))
                .expectNext(waiverId)
                .verifyComplete();
    }

    @Test
    void removeWaiverHandler_invokesDeleteConfiguration() {
        RemoveWaiverHandler handler = new RemoveWaiverHandler(api);
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();

        when(api.deleteConfiguration(eq(productId), eq(waiverId), any())).thenReturn(Mono.empty());

        StepVerifier.create(handler.handle(new RemoveWaiverCommand(productId, waiverId)))
                .verifyComplete();

        verify(api, times(1)).deleteConfiguration(eq(productId), eq(waiverId), any());
    }

    @Test
    void searchWaiversHandler_deserializesAndFiltersByCode() {
        SearchWaiversHandler handler = new SearchWaiversHandler(api, objectMapper);
        UUID productId = UUID.randomUUID();
        UUID waiverId = UUID.randomUUID();

        ProductConfigurationDTO cfg = new ProductConfigurationDTO(
                LocalDateTime.now(), LocalDateTime.now(), waiverId)
                .productId(productId)
                .configType(ProductConfigurationDTO.ConfigTypeEnum.CUSTOM)
                .configKey("WAIVER_PROMO10")
                .configValue("{\"spec\":{\"code\":\"PROMO10\",\"description\":\"10% off\",\"adjustment\":-0.01}}");

        when(api.filterConfigurations(eq(productId), any(FilterRequestProductConfigurationDTO.class), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of(cfg))));

        StepVerifier.create(handler.handle(new WaiverSearchQuery(productId, "PROMO", null)))
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).waiverId()).isEqualTo(waiverId);
                    assertThat(list.get(0).spec().code()).isEqualTo("PROMO10");
                })
                .verifyComplete();
    }

    @Test
    void searchWaiversHandler_filtersOutNonMatchingCode() {
        SearchWaiversHandler handler = new SearchWaiversHandler(api, objectMapper);
        UUID productId = UUID.randomUUID();

        ProductConfigurationDTO cfg = new ProductConfigurationDTO()
                .productId(productId)
                .configType(ProductConfigurationDTO.ConfigTypeEnum.CUSTOM)
                .configKey("WAIVER_SUMMER")
                .configValue("{\"spec\":{\"code\":\"SUMMER\"}}");

        when(api.filterConfigurations(eq(productId), any(), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of(cfg))));

        StepVerifier.create(handler.handle(new WaiverSearchQuery(productId, "WINTER", null)))
                .assertNext(list -> assertThat(list).isEmpty())
                .verifyComplete();
    }

    private static void assertUuid(String value) {
        assertThat(value).isNotNull().isNotBlank();
        UUID.fromString(value);
    }
}
