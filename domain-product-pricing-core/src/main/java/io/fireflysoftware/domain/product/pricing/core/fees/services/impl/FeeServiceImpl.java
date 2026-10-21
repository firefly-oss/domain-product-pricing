package io.fireflysoftware.domain.product.pricing.core.fees.services.impl;

import io.fireflysoftware.domain.product.pricing.core.fees.commands.RegisterFeeSchemaCommand;
import io.fireflysoftware.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import io.fireflysoftware.domain.product.pricing.core.fees.services.FeesService;
import io.fireflysoftware.domain.product.pricing.core.fees.workflows.RegisterFeeSchemaSaga;
import io.fireflysoftware.domain.product.pricing.core.fees.workflows.UpdateFeeRuleSaga;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FeeServiceImpl implements FeesService {

    private final SagaEngine engine;

    @Autowired
    public FeeServiceImpl(SagaEngine engine){
        this.engine=engine;
    }


    @Override
    public Mono<SagaResult> defineFeeScheme(RegisterFeeSchemaCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId("registerProductFeeStructure", command.getProductFeeStructure())
                .build();
        return engine.execute("RegisterFeeSchemaSaga", inputs);
    }

    @Override
    public Mono<SagaResult> updateFeeRule(UpdateFeeApplicationRuleCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId("readFeeRule", command)
                .forStepId("updateFee", command)
                .build();
        return engine.execute("UpdateFeeRuleSaga", inputs);
    }
}
