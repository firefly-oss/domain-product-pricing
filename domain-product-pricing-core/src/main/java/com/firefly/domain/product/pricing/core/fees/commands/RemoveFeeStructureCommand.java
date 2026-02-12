package com.firefly.domain.product.pricing.core.fees.commands;

import org.fireflyframework.cqrs.command.Command;

import java.util.UUID;

public record RemoveFeeStructureCommand(
        UUID productId,
        UUID feeStructureId
) implements Command<Void>{}
