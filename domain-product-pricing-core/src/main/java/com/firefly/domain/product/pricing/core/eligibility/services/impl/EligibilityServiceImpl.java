package com.firefly.domain.product.pricing.core.eligibility.services.impl;

import com.firefly.domain.product.pricing.core.eligibility.commands.AdjustEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.EvaluateEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.commands.PublishEligibilityCommand;
import com.firefly.domain.product.pricing.core.eligibility.services.EligibilityService;
import org.fireflyframework.transactional.saga.core.SagaResult;
import org.fireflyframework.transactional.saga.engine.SagaEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private final SagaEngine engine;

    @Autowired
    public EligibilityServiceImpl(SagaEngine engine){
        this.engine=engine;
    }


    @Override
    public Mono<SagaResult> publishEligibility(PublishEligibilityCommand command) {
        return null;
    }

    @Override
    public Mono<SagaResult> adjustEligibility(AdjustEligibilityCommand command) {
        return null;
    }

    @Override
    public Mono<SagaResult> evaluateEligibility(EvaluateEligibilityCommand command) {
        return null;
    }
}
