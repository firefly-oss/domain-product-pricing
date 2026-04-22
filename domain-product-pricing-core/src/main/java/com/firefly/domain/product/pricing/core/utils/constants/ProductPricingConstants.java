package com.firefly.domain.product.pricing.core.utils.constants;

public class ProductPricingConstants {

    // ============================== SAGA CONFIGURATION ==============================
    public static final String SAGA_REGISTER_PRICING = "RegisterPricingSaga";
    public static final String SAGA_UPDATE_PRICING = "UpdatePricingSaga";
    public static final String SAGA_REGISTER_FEE_SCHEMA = "RegisterFeeSchemaSaga";
    public static final String SAGA_UPDATE_FEE = "UpdateFeeSaga";


    // ============================== STEP IDENTIFIERS ==============================
    public static final String STEP_REGISTER_PRODUCT_PRICING = "registerProductPricing";
    public static final String STEP_UPDATE_PRICING = "updatePricing";
    public static final String STEP_REGISTER_FEE_STRUCTURE = "registerFeeStructure";
    public static final String STEP_REGISTER_FEE_COMPONENT = "registerFeeComponent";
    public static final String STEP_REGISTER_FEE_APPLICATION_RULE = "registerFeeApplicationRule";
    public static final String STEP_REGISTER_PRODUCT_FEE_STRUCTURE = "registerProductFeeStructure";
    public static final String STEP_UPDATE_FEE = "updateFee";


    // ============================== COMPENSATE METHODS ==============================
    public static final String COMPENSATE_REMOVE_FEE_STRUCTURE = "removeFeeStructure";
    public static final String COMPENSATE_REMOVE_FEE_COMPONENT = "removeFeeComponent";
    public static final String COMPENSATE_REMOVE_FEE_APPLICATION_RULE = "removeFeeApplicationRule";
    public static final String COMPENSATE_REMOVE_PRODUCT_FEE_STRUCTURE = "removeProductFeeStructure";


    // ============================== EVENT TYPES ==============================
    public static final String EVENT_PRODUCT_PRICING_REGISTERED = "productPricing.registered";
    public static final String EVENT_PRICING_UPDATED = "pricing.updated";
    public static final String EVENT_FEE_STRUCTURE_REGISTERED = "feeStructure.registered";
    public static final String EVENT_FEE_COMPONENT_REGISTERED = "feeComponent.registered";
    public static final String EVENT_FEE_APPLICATION_RULE_REGISTERED = "feeApplicationRule.registered";
    public static final String EVENT_PRODUCT_FEE_STRUCTURE_REGISTERED = "productFeeStructure.registered";
    public static final String EVENT_FEE_UPDATED = "fee.updated";

    // ============================== CONFIGURATION TYPES (Step 3C.1) ==============================
    public static final String CONFIG_TYPE_PRICING_WAIVER = "PRICING_WAIVER";
    public static final String CONFIG_TYPE_PRICING_HISTORY = "PRICING_HISTORY";

    // ============================== COMMAND / QUERY NAMES (Step 3C.1) ==============================
    public static final String CMD_CREATE_WAIVER = "create-waiver";
    public static final String CMD_UPDATE_WAIVER = "update-waiver";
    public static final String CMD_REMOVE_WAIVER = "remove-waiver";

    // ============================== AUDIT EVENT TYPES (Step 3C.1) ==============================
    public static final String EVENT_WAIVER_CREATED = "pricing.waiver.created";
    public static final String EVENT_WAIVER_REMOVED = "pricing.waiver.removed";
    public static final String EVENT_SCHEME_IMPACT_SIMULATED = "pricing.scheme-impact.simulated";


}
