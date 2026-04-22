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

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Best-effort audit timeline for a pricing entity. Until the platform adopts a
 * centralized audit store, the timeline is reconstructed from
 * {@code ProductVersion} records and the configuration's own
 * {@code dateCreated} / {@code dateUpdated} timestamps.
 */
public interface PricingHistoryService {

    /**
     * @param entityId configuration id (waiver, pricing scheme)
     * @param from lower bound (inclusive), optional
     * @param to upper bound (inclusive), optional
     */
    Mono<PricingHistoryResponse> getHistory(UUID entityId, Optional<LocalDate> from, Optional<LocalDate> to);
}
