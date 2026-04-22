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

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Audit timeline for a pricing entity.")
public record PricingHistoryResponse(
        @Schema(description = "Pricing entity identifier (configuration id).") UUID entityId,
        @Schema(description = "Entity type inferred from the configuration, e.g. PRICING_WAIVER.") String entityType,
        @Schema(description = "Chronological audit entries.") List<HistoryEntry> entries
) {}
