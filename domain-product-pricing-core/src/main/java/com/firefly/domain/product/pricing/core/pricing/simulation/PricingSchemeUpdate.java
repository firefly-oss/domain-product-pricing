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

/**
 * Proposed change to a pricing scheme fed into the impact simulator. Leaving a
 * field null means "keep current value" for that dimension.
 */
@Schema(description = "Proposed pricing scheme change fed into the impact simulator.")
public record PricingSchemeUpdate(
        @Schema(description = "New nominal annual interest rate (decimal, e.g. 0.045).")
        BigDecimal newInterestRate,
        @Schema(description = "New fixed fee applied per transaction.")
        BigDecimal newFixedFee,
        @Schema(description = "New effective APR disclosed to customers.")
        BigDecimal newEffectiveApr
) {}
