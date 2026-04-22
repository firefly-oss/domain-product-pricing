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

package com.firefly.domain.product.pricing.web.controller;

import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.waivers.WaiverSpec;
import com.firefly.domain.product.pricing.core.waivers.WaiverSummary;
import com.firefly.domain.product.pricing.core.waivers.commands.CreateWaiverCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing/waivers")
@RequiredArgsConstructor
@Tag(name = "Waivers", description = "Pricing waivers / promotional discounts stored as product configurations.")
public class WaiversController {

    private final PricingService pricingService;

    @Operation(summary = "Create waiver",
            description = "Attaches a pricing waiver to a product.")
    @ApiResponse(responseCode = "200", description = "Created; waiver id returned in the response body.")
    @PostMapping
    public Mono<ResponseEntity<UUID>> createWaiver(@Valid @RequestBody CreateWaiverCommand command) {
        return pricingService.createWaiver(command.productId(), command.spec(), command.tenantId())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Update waiver",
            description = "Replaces the spec of an existing waiver. The path productId ensures cross-tenant isolation.")
    @ApiResponse(responseCode = "200", description = "Updated.")
    @PutMapping("/{waiverId}")
    public Mono<ResponseEntity<UUID>> updateWaiver(@PathVariable UUID waiverId,
                                                   @RequestParam UUID productId,
                                                   @Valid @RequestBody WaiverSpec spec) {
        return pricingService.updateWaiver(productId, waiverId, spec)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Remove waiver",
            description = "Deletes a waiver configuration.")
    @ApiResponse(responseCode = "200", description = "Deleted.")
    @DeleteMapping("/{waiverId}")
    public Mono<ResponseEntity<Void>> removeWaiver(@PathVariable UUID waiverId,
                                                   @RequestParam UUID productId) {
        return pricingService.removeWaiver(productId, waiverId)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @Operation(summary = "Search waivers",
            description = "Searches waivers for a product, optionally filtering by code substring and active-on date.")
    @ApiResponse(responseCode = "200", description = "Matching waiver summaries.")
    @GetMapping("/search")
    public Mono<ResponseEntity<List<WaiverSummary>>> searchWaivers(
            @RequestParam UUID productId,
            @RequestParam(required = false) String codeContains,
            @RequestParam(required = false) LocalDate activeOn) {
        return pricingService.searchWaivers(productId, codeContains, activeOn)
                .map(ResponseEntity::ok);
    }
}
