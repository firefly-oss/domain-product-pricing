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


package io.fireflysoftware.domain.product.pricing.core.eligibility.services;

import io.fireflysoftware.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import io.fireflysoftware.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import io.fireflysoftware.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import io.fireflysoftware.domain.product.pricing.core.eligibility.results.EligibilityResultDTO;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

public interface EligibilityService {


    /**
     * Publishes eligibility rules by processing the command containing eligibility criteria.
     *
     * @param command an instance of {@code PublishEligibilityCommand} containing the eligibility criteria to be published
     * @return a {@code Mono<SagaResult>} indicating the result of the eligibility publication process
     */
    Mono<SagaResult> publishEligibility(@Valid PublishEligibilityCommand command);

    /**
     * Adjusts an existing eligibility configuration based on the provided command.
     *
     * @param command an instance of {@code AdjustEligibilityCommand} containing the details required to adjust the eligibility configuration
     * @return a {@code Mono<SagaResult>} representing the result of the adjustment process
     */
    Mono<SagaResult> adjustEligibility(@Valid AdjustEligibilityCommand command);

    /**
     * Evaluates eligibility based on applicant facts and determines their fit with the eligibility criteria.
     *
     * @param command applicant facts ({@code partyId}, {@code productId}, {@code requestedAmount})
     * @return outcome of the evaluation including {@code eligible}, {@code maxAmount} and failure {@code reasons}
     */
    Mono<EligibilityResultDTO> evaluateEligibility(@Valid EvaluateEligibilityCommand command);
}
