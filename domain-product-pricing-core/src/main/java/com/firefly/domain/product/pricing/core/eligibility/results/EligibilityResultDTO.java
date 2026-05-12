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

package com.firefly.domain.product.pricing.core.eligibility.results;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Outcome of a product eligibility evaluation")
public class EligibilityResultDTO {

    @Schema(description = "Identifier assigned to this evaluation", example = "0c3a5d3b-7c2c-4dca-8e2f-1e4d8a4d0b1a")
    private UUID evaluationId;

    @Schema(description = "Whether the applicant is eligible for the product with the requested amount", example = "true")
    private boolean eligible;

    @Schema(description = "Maximum amount the product allows for this applicant", example = "60000")
    private BigDecimal maxAmount;

    @Schema(description = "Codes describing why the applicant is not eligible (empty when eligible)",
            example = "[\"AMOUNT_ABOVE_MAXIMUM\"]")
    private List<String> reasons;
}
