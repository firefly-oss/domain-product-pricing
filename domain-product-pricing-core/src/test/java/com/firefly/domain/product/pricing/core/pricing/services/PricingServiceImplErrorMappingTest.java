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

package com.firefly.domain.product.pricing.core.pricing.services;

import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryService;
import com.firefly.domain.product.pricing.core.pricing.services.impl.PricingServiceImpl;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactSimulationService;
import com.firefly.domain.product.pricing.core.waivers.queries.WaiverSearchQuery;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.web.error.exceptions.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that downstream transport failures surface as HTTP 503 (via
 * {@link ServiceUnavailableException}) instead of the generic 500 emitted by
 * the CQRS {@code QueryProcessingException} wrapper — matching the behaviour
 * of non-CQRS flows like {@code getCatalogTree}.
 */
class PricingServiceImplErrorMappingTest {

    private QueryBus queryBus;
    private PricingServiceImpl service;

    @BeforeEach
    void setUp() {
        queryBus = mock(QueryBus.class);
        service = new PricingServiceImpl(
                mock(SagaEngine.class),
                mock(CommandBus.class),
                queryBus,
                mock(SchemeImpactSimulationService.class),
                mock(PricingHistoryService.class));
    }

    @Test
    void searchWaivers_mapsWrappedConnectExceptionTo503() {
        RuntimeException queryProcessingException = new RuntimeException(
                "Failed to process query", new ConnectException("Connection refused"));
        when(queryBus.<java.util.List<com.firefly.domain.product.pricing.core.waivers.WaiverSummary>>query(any(WaiverSearchQuery.class)))
                .thenReturn(Mono.error(queryProcessingException));

        StepVerifier.create(service.searchWaivers(UUID.randomUUID(), null, null))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(ServiceUnavailableException.class);
                    assertThat(((ServiceUnavailableException) err).getServiceName())
                            .isEqualTo("core-common-product-mgmt");
                })
                .verify();
    }

    @Test
    void searchWaivers_mapsWrappedWebClientRequestExceptionTo503() {
        WebClientRequestException wcre = new WebClientRequestException(
                new java.net.UnknownHostException("dns-fail"),
                HttpMethod.POST,
                URI.create("http://core-common-product-mgmt/filter"),
                new org.springframework.http.HttpHeaders());
        RuntimeException queryProcessingException = new RuntimeException("Failed to process query", wcre);
        when(queryBus.<java.util.List<com.firefly.domain.product.pricing.core.waivers.WaiverSummary>>query(any(WaiverSearchQuery.class)))
                .thenReturn(Mono.error(queryProcessingException));

        StepVerifier.create(service.searchWaivers(UUID.randomUUID(), null, null))
                .expectError(ServiceUnavailableException.class)
                .verify();
    }

    @Test
    void searchWaivers_passesThroughUnrelatedErrors() {
        RuntimeException other = new RuntimeException("something else entirely");
        when(queryBus.<java.util.List<com.firefly.domain.product.pricing.core.waivers.WaiverSummary>>query(any(WaiverSearchQuery.class)))
                .thenReturn(Mono.error(other));

        StepVerifier.create(service.searchWaivers(UUID.randomUUID(), null, null))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isNotInstanceOf(ServiceUnavailableException.class);
                    assertThat(err.getMessage()).contains("something else entirely");
                })
                .verify();
    }

}
