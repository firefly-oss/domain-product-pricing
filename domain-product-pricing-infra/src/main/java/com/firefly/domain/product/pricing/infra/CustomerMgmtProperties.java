package com.firefly.domain.product.pricing.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties.
 * Maps the properties defined in application.yaml under api-configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "api-configuration.common-platform.customer-mgmt")
@Data
public class CustomerMgmtProperties {

    private String basePath;

}
