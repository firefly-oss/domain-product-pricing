package com.firefly.domain.product.pricing.web.controller;

import com.firefly.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.results.EligibilityResultDTO;
import com.firefly.domain.product.pricing.core.eligibility.services.EligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing/eligibility")
@RequiredArgsConstructor
@Tag(name = "Eligibility", description = "CQ queries and registration for product eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    // --- Eligibility ---
    @Operation(summary = "Publish eligibility criteria", description = "Publish eligibility rules (KYC/KYB, score, income, activity).")
    @PostMapping
    public Mono<ResponseEntity<Object>> publishEligibility(@Valid @RequestBody PublishEligibilityCommand command) {
        return eligibilityService.publishEligibility(command)
                .thenReturn(ResponseEntity.ok().build());
    }

    @Operation(summary = "Adjust eligibility criteria", description = "Adjust criteria with versioning.")
    @PatchMapping("/{eligibilityId}")
    public Mono<ResponseEntity<Object>> adjustEligibility(@PathVariable UUID eligibilityId,
                                                          @Valid @RequestBody AdjustEligibilityCommand command) {
        return eligibilityService.adjustEligibility(command)
                .thenReturn(ResponseEntity.ok().build());
    }

    @Operation(summary = "Evaluate eligibility", description = "Evaluate applicant facts and return fit/not-fit with reasons.")
    @PostMapping("/{eligibilityId}/evaluate")
    public Mono<ResponseEntity<EligibilityResultDTO>> evaluateEligibility(@PathVariable UUID eligibilityId,
                                                                          @Valid @RequestBody EvaluateEligibilityCommand command) {
        return eligibilityService.evaluateEligibility(command)
                .map(ResponseEntity::ok);
    }
}
