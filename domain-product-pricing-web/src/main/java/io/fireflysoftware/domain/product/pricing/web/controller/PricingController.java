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


package io.fireflysoftware.domain.product.pricing.web.controller;

import io.fireflysoftware.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import io.fireflysoftware.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import io.fireflysoftware.domain.product.pricing.core.pricing.services.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "CQ queries and registration for product pricing")
public class PricingController {

    private final PricingService pricingService;

    // --- Pricing ---
    @Operation(summary = "Register pricing", description = "Create rates with tiers and effectiveFrom date.")
    @PostMapping
    public Mono<ResponseEntity<Object>> registerPricing(@Valid @RequestBody RegisterProductPricingCommand command) {
        return pricingService.registerPricing(command)
                .thenReturn(ResponseEntity.ok().build());
    }

    @Operation(summary = "Amend pricing", description = "Amend rates/margins/tiers by creating a new effective version.")
    @PutMapping("/{pricingId}")
    public Mono<ResponseEntity<Object>> amendPricing(@PathVariable UUID pricingId,
                                                     @Valid @RequestBody UpdateProductPricingCommand command) {
        return pricingService.amendPricing(command.withProductPricingId(pricingId))
                .thenReturn(ResponseEntity.ok().build());
    }

}
