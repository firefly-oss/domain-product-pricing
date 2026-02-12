package com.firefly.domain.product.pricing.core.pricing.services.impl;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.pricing.workflows.RegisterPricingSaga;
import com.firefly.domain.product.pricing.core.pricing.workflows.UpdatePricingSaga;
import org.fireflyframework.transactional.saga.core.SagaResult;
import org.fireflyframework.transactional.saga.engine.SagaEngine;
import org.fireflyframework.transactional.saga.engine.StepInputs;
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
                .forStep(RegisterPricingSaga::registerPricing, registerProductPricingCommand)
                .build();
        return engine.execute(RegisterPricingSaga.class, inputs);
    }

    @Override
    public Mono<SagaResult> amendPricing(UpdateProductPricingCommand updateProductPricingCommand) {
        StepInputs inputs = StepInputs.builder()
                .forStep(UpdatePricingSaga::updatePricing, updateProductPricingCommand)
                .build();
        return engine.execute(UpdatePricingSaga.class, inputs);
    }
}
