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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/v1/pricing/schemes/{id}/simulate-impact}.
 * The scheme id is carried in the URL path.
 */
@Schema(description = "Impact-simulation request: proposed change to a pricing scheme.")
public record SimulateSchemeImpactRequest(
        @Valid @NotNull
        @Schema(description = "Proposed change to the scheme being simulated.")
        PricingSchemeUpdate proposedChange
) {}
