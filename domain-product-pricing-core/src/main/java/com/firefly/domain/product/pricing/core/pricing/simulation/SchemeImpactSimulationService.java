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

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Projects the impact of a pricing scheme change across the product portfolio.
 * Read-only aggregation — no downstream writes, no saga.
 */
public interface SchemeImpactSimulationService {

    /**
     * Simulate the effect of {@code proposed} on all products referencing the
     * given scheme under a reference scenario (fixed amount + tenor).
     */
    Mono<SchemeImpactReport> simulate(UUID schemeId, PricingSchemeUpdate proposed);
}
