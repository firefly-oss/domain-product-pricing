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


package io.fireflysoftware.domain.product.pricing.core.fees.services;

import io.fireflysoftware.domain.product.pricing.core.fees.commands.RegisterFeeSchemaCommand;
import io.fireflysoftware.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
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
