package com.firefly.domain.product.pricing.infra;

import com.firefly.core.customer.sdk.api.PartiesApi;
import com.firefly.core.customer.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Creates reactive API client beans for the core-common-customer-mgmt downstream service.
 */
@Component
public class CustomerMgmtClientFactory {

    private final ApiClient apiClient;

    public CustomerMgmtClientFactory(CustomerMgmtProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    @Bean
    public PartiesApi partiesApi() {
        return new PartiesApi(apiClient);
    }
}
