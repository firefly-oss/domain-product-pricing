package com.firefly.domain.product.pricing.core.fees.services.impl;

import com.firefly.domain.product.pricing.core.fees.commands.RegisterFeeSchemaCommand;
import com.firefly.domain.product.pricing.core.fees.commands.UpdateFeeApplicationRuleCommand;
import com.firefly.domain.product.pricing.core.fees.services.FeesService;
import com.firefly.domain.product.pricing.core.fees.workflows.RegisterFeeSchemaSaga;
import com.firefly.domain.product.pricing.core.fees.workflows.UpdateFeeRuleSaga;
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
                .forStepId("registerFeeStructure", command.getFeeStructure())
                .forStepId("registerFeeComponent", command.getFeeComponent())
                .forStepId("registerFeeApplicationRule", command.getFeeApplicationRule())
                .forStepId("registerProductFeeStructure", command.getProductFeeStructure())
                .build();
        return engine.execute("RegisterFeeSchemaSaga", inputs);
    }

    @Override
    public Mono<SagaResult> updateFeeRule(UpdateFeeApplicationRuleCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId("updateFee", command)
                .build();
        return engine.execute("UpdateFeeRuleSaga", inputs);
    }
}
