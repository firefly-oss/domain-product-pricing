package com.firefly.domain.product.pricing.core.fees.services;

import com.firefly.domain.product.pricing.core.fees.commands.RegisterFeeSchemaCommand;
import com.firefly.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import org.fireflyframework.transactional.saga.core.SagaResult;
import reactor.core.publisher.Mono;

public interface FeesService {


    /**
     * Defines a fee scheme, including the structure, components, and application rules.
     *
     * @param command the {@link RegisterFeeSchemaCommand} containing the details
     *                of the fee scheme to be registered, such as application
     *                rules, fee components, and product-specific fee structures.
     * @return a {@link Mono} emitting {@code SagaResult} which represents the result
     *         of the Saga orchestration process for defining the fee scheme.
     */
    Mono<SagaResult> defineFeeScheme(RegisterFeeSchemaCommand command);

    /**
     * Updates a specific fee calculation rule based on the provided command details.
     *
     * @param command the request object containing information about the fee rule
     *                to be updated, including identifiers for the fee application rule
     *                and fee component.
     * @return a Mono wrapping the result of the update operation, represented as
     *         a SagaResult which reflects the success or failure of the operation.
     */
    Mono<SagaResult> updateFeeRule(UpdateFeeApplicationRuleCommand command);

}
