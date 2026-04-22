package com.firefly.domain.product.pricing.core.pricing.services.impl;

import com.firefly.domain.product.pricing.core.pricing.commands.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.commands.UpdateProductPricingCommand;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryResponse;
import com.firefly.domain.product.pricing.core.pricing.history.PricingHistoryService;
import com.firefly.domain.product.pricing.core.pricing.services.PricingService;
import com.firefly.domain.product.pricing.core.pricing.simulation.PricingSchemeUpdate;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactReport;
import com.firefly.domain.product.pricing.core.pricing.simulation.SchemeImpactSimulationService;
import com.firefly.domain.product.pricing.core.waivers.WaiverSpec;
import com.firefly.domain.product.pricing.core.waivers.WaiverSummary;
import com.firefly.domain.product.pricing.core.waivers.commands.CreateWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.commands.RemoveWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.commands.UpdateWaiverCommand;
import com.firefly.domain.product.pricing.core.waivers.queries.WaiverSearchQuery;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.fireflyframework.web.error.exceptions.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PricingServiceImpl implements PricingService {

    private final SagaEngine engine;
    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final SchemeImpactSimulationService schemeImpactSimulationService;
    private final PricingHistoryService pricingHistoryService;

    @Autowired
    public PricingServiceImpl(SagaEngine engine,
                              CommandBus commandBus,
                              QueryBus queryBus,
                              SchemeImpactSimulationService schemeImpactSimulationService,
                              PricingHistoryService pricingHistoryService) {
        this.engine = engine;
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.schemeImpactSimulationService = schemeImpactSimulationService;
        this.pricingHistoryService = pricingHistoryService;
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

    @Override
    public Mono<SchemeImpactReport> simulateSchemeImpact(UUID schemeId, PricingSchemeUpdate proposed) {
        return schemeImpactSimulationService.simulate(schemeId, proposed);
    }

    @Override
    public Mono<UUID> createWaiver(UUID productId, WaiverSpec spec, UUID tenantId) {
        return commandBus.send(new CreateWaiverCommand(productId, spec, tenantId));
    }

    @Override
    public Mono<UUID> updateWaiver(UUID productId, UUID waiverId, WaiverSpec spec) {
        return commandBus.send(new UpdateWaiverCommand(productId, waiverId, spec));
    }

    @Override
    public Mono<Void> removeWaiver(UUID productId, UUID waiverId) {
        return commandBus.send(new RemoveWaiverCommand(productId, waiverId));
    }

    @Override
    public Mono<List<WaiverSummary>> searchWaivers(UUID productId, String codeContains, LocalDate activeOn) {
        return queryBus.query(new WaiverSearchQuery(productId, codeContains, activeOn))
                .onErrorMap(PricingServiceImpl::mapDownstreamUnavailable);
    }

    /**
     * Walks the cause chain looking for a transport-level failure against the
     * {@code core-common-product-mgmt} service. When found, maps to a framework
     * {@link ServiceUnavailableException} so the shared error converters emit
     * HTTP 503 instead of the generic HTTP 500 produced by the CQRS
     * {@code QueryProcessingException} wrapper.
     */
    private static Throwable mapDownstreamUnavailable(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof WebClientRequestException || cur instanceof ConnectException) {
                return ServiceUnavailableException.forService("core-common-product-mgmt");
            }
            cur = cur.getCause();
        }
        return ex;
    }

    @Override
    public Mono<PricingHistoryResponse> getPricingHistory(UUID entityId, Optional<LocalDate> from, Optional<LocalDate> to) {
        return pricingHistoryService.getHistory(entityId, from, to);
    }
}
