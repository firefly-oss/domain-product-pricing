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

import io.fireflysoftware.domain.product.pricing.core.fees.commands.RegisterFeeSchemaCommand;
import io.fireflysoftware.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import io.fireflysoftware.domain.product.pricing.core.fees.services.FeesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pricing/fees")
@RequiredArgsConstructor
@Tag(name = "Fees", description = "CQ queries and registration for product fees")
public class FeesController {

    private final FeesService feesService;

    // --- Fees ---
    @Operation(summary = "Define fee scheme", description = "Define fee types and calculation rules.")
    @PostMapping("/schemes")
    public Mono<ResponseEntity<Object>> defineFeeScheme(@Valid @RequestBody RegisterFeeSchemaCommand command) {
        return feesService.defineFeeScheme(command)
                .thenReturn(ResponseEntity.ok().build());
    }

    @Operation(summary = "Update fee rule", description = "Update a specific fee calculation rule.")
    @PutMapping("/schemes/{schemeId}/components/{componentId}")
    public Mono<ResponseEntity<Object>> updateFeeRule(@PathVariable UUID schemeId,
                                                      @PathVariable UUID componentId,
                                                      @Valid @RequestBody UpdateFeeApplicationRuleCommand command) {
        return feesService.updateFeeRule(command
                        .withFeeApplicationRuleId(schemeId)
                        .withFeeComponentId(componentId))
                .thenReturn(ResponseEntity.ok().build());
    }

}
