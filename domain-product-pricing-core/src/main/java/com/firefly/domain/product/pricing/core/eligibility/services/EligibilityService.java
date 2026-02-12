package com.firefly.domain.product.pricing.core.eligibility.services;

import com.firefly.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import org.fireflyframework.transactional.saga.core.SagaResult;
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
     * This method processes the provided {@code EvaluateEligibilityCommand} and returns the evaluation result.
     *
     * @param command an instance of {@code EvaluateEligibilityCommand} containing the necessary data for evaluating eligibility
     * @return a {@code Mono<SagaResult>} representing the outcome of the eligibility evaluation, including reasons for fit/not-fit
     */
    Mono<SagaResult> evaluateEligibility(@Valid EvaluateEligibilityCommand command);
}
