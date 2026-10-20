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
