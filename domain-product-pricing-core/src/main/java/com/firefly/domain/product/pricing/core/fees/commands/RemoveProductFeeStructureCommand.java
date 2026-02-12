package com.firefly.domain.product.pricing.core.fees.commands;

import org.fireflyframework.cqrs.command.Command;

import java.util.UUID;

public record RemoveProductFeeStructureCommand(
        UUID productId,
        UUID productFeeStructureId
) implements Command<Void>{}