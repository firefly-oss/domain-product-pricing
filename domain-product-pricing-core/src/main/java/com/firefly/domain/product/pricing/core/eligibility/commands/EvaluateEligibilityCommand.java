/*
 * Copyright 2025 Firefly Software Foundation
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

package com.firefly.domain.product.pricing.core.eligibility.commands;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.fireflyframework.cqrs.command.Command;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "Applicant facts used to evaluate eligibility for a product")
public class EvaluateEligibilityCommand implements Command<UUID> {

    @NotNull
    @Schema(description = "Identifier of the applicant party", example = "6c04e2bc-5523-4bc7-9002-528f2dbe3229")
    private UUID partyId;

    @NotNull
    @Schema(description = "Identifier of the product being evaluated", example = "00000000-0000-0000-0000-00000000000a")
    private UUID productId;

    @NotNull
    @Positive
    @Schema(description = "Principal amount the applicant is requesting", example = "10000")
    private BigDecimal requestedAmount;
}
