package com.firefly.domain.product.pricing.infra;

import com.firefly.core.product.sdk.api.ProductConfigurationApi;
import com.firefly.core.product.sdk.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the ClientFactory interface.
 * Creates client service instances using the appropriate API clients and dependencies.
 */
@Component
public class ClientFactory {

    private final ApiClient apiClient;

    @Autowired
    public ClientFactory(
            ProductMgmtProperties productMgmtProperties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(productMgmtProperties.getBasePath());
    }

    @Bean
    public ProductConfigurationApi productConfigurationApi() {
        return new ProductConfigurationApi(apiClient);
    }

}
