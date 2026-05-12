package com.firefly.domain.product.pricing.core.eligibility.services;

import com.firefly.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.results.EligibilityResultDTO;
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
