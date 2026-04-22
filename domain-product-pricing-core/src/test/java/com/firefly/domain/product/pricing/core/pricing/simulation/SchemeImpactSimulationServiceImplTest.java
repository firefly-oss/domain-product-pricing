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

package com.firefly.domain.product.pricing.core.pricing.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.model.FilterRequestProductConfigurationDTO;
import com.firefly.core.product.sdk.model.PaginationResponse;
import com.firefly.core.product.sdk.model.ProductConfigurationDTO;
import com.firefly.domain.product.pricing.core.pricing.simulation.impl.SchemeImpactSimulationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemeImpactSimulationServiceImplTest {

    private ProductConfigurationApi productConfigurationApi;
    private SchemeImpactSimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        productConfigurationApi = mock(ProductConfigurationApi.class);
        service = new SchemeImpactSimulationServiceImpl(productConfigurationApi, new ObjectMapper());
    }

    @Test
    void simulate_noMatchingConfigs_returnsZeroAffected() {
        UUID schemeId = UUID.randomUUID();
        when(productConfigurationApi.filterConfigurations(eq(schemeId), any(FilterRequestProductConfigurationDTO.class), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of())));

        StepVerifier.create(service.simulate(schemeId,
                        new PricingSchemeUpdate(new BigDecimal("0.06"), null, null)))
                .assertNext(report -> {
                    assertThat(report.affectedProductCount()).isZero();
                    assertThat(report.productImpacts()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void simulate_oneAffectedProduct_reportsImpactAndStats() {
        UUID schemeId = UUID.randomUUID();
        ProductConfigurationDTO cfg = new ProductConfigurationDTO()
                .productId(schemeId)
                .configType(ProductConfigurationDTO.ConfigTypeEnum.PRICING)
                .configKey("PRICING_SCHEME")
                .configValue("{\"interestRate\": 0.04}");

        when(productConfigurationApi.filterConfigurations(eq(schemeId), any(), any()))
                .thenReturn(Mono.just(new PaginationResponse().content(List.of(cfg))));

        PricingSchemeUpdate proposed = new PricingSchemeUpdate(new BigDecimal("0.06"), null, null);

        StepVerifier.create(service.simulate(schemeId, proposed))
                .assertNext(report -> {
                    assertThat(report.affectedProductCount()).isEqualTo(1);
                    assertThat(report.productImpacts()).hasSize(1);
                    ProductImpact impact = report.productImpacts().get(0);
                    assertThat(impact.currentMonthlyPayment()).isPositive();
                    assertThat(impact.newMonthlyPayment()).isGreaterThan(impact.currentMonthlyPayment());
                    assertThat(report.stats().avgDeltaPercent()).isPositive();
                })
                .verifyComplete();
    }
}
