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

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Impact of a pricing scheme change on a single product.")
public record ProductImpact(
        @Schema(description = "Product identifier.") UUID productId,
        @Schema(description = "Current monthly payment under the existing scheme (reference scenario).")
        BigDecimal currentMonthlyPayment,
        @Schema(description = "Projected monthly payment under the proposed scheme.")
        BigDecimal newMonthlyPayment,
        @Schema(description = "Relative delta, e.g. 0.05 means +5%.") BigDecimal deltaPercent
) {}
