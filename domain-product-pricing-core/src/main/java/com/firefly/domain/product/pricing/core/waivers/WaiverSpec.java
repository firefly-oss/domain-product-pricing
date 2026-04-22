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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Full specification for a pricing waiver (promotion / discount). Serialized as
 * the JSON payload of a {@code ProductConfiguration} entry with
 * {@code configType = PRICING_WAIVER}.
 */
@Schema(description = "Full specification for a pricing waiver.")
public record WaiverSpec(
        @NotBlank @Size(max = 40)
        @Schema(description = "Short waiver code (alphanumeric + underscore). Used as the config key suffix.")
        String code,
        @Size(max = 500)
        @Schema(description = "Human-readable description.") String description,
        @Schema(description = "Optional eligibility filter expression (JSON blob interpreted by pricing engine).")
        String eligibilityFilter,
        @Schema(description = "Adjustment applied when the waiver is triggered (e.g. -0.005 for -50 bps).")
        BigDecimal adjustment,
        @Schema(description = "Start date (inclusive).") LocalDate validFrom,
        @Schema(description = "End date (inclusive).") LocalDate validTo
) {}
