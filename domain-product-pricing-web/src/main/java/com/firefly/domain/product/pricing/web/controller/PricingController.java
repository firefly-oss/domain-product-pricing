package com.firefly.domain.product.pricing.web.controller;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryResponse;
import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactReport;
import com.firefly.domain.product.pricing.core.pricing.simulation.SimulateSchemeImpactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;
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

    // --- Phase 5C.1 additions ---

    @Operation(summary = "Simulate scheme impact",
            description = "Projects portfolio-wide impact of a proposed pricing scheme change.")
    @ApiResponse(responseCode = "200", description = "Per-product deltas and aggregate statistics.")
    @PostMapping("/schemes/{id}/simulate-impact")
    public Mono<ResponseEntity<SchemeImpactReport>> simulateSchemeImpact(
            @PathVariable("id") UUID schemeId,
            @Valid @RequestBody SimulateSchemeImpactRequest request) {
        return pricingService.simulateSchemeImpact(schemeId, request.proposedChange())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get pricing history",
            description = "Best-effort audit timeline for a pricing entity (waiver, scheme).")
    @ApiResponse(responseCode = "200", description = "Timeline entries, oldest first.")
    @GetMapping("/history/{entityId}")
    public Mono<ResponseEntity<PricingHistoryResponse>> getPricingHistory(
            @PathVariable UUID entityId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return pricingService.getPricingHistory(entityId, Optional.ofNullable(from), Optional.ofNullable(to))
                .map(ResponseEntity::ok);
    }

}
