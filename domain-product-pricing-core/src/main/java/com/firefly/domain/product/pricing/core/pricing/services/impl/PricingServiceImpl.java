package com.firefly.domain.product.pricing.core.pricing.services.impl;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.pricing.workflows.RegisterPricingSaga;
import com.firefly.domain.product.pricing.core.pricing.workflows.UpdatePricingSaga;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PricingServiceImpl implements PricingService {

    private final SagaEngine engine;

    @Autowired
    public PricingServiceImpl(SagaEngine engine){
        this.engine=engine;
    }

    @Override
    public Mono<SagaResult> registerPricing(RegisterProductPricingCommand registerProductPricingCommand) {
        StepInputs inputs = StepInputs.builder()
                .forStepId("registerProductPricing", registerProductPricingCommand)
                .build();
        return engine.execute("RegisterPricingSaga", inputs);
    }

    @Override
    public Mono<SagaResult> amendPricing(UpdateProductPricingCommand updateProductPricingCommand) {
        StepInputs inputs = StepInputs.builder()
                .forStepId("updatePricing", updateProductPricingCommand)
                .build();
        return engine.execute("UpdatePricingSaga", inputs);
    }
}
