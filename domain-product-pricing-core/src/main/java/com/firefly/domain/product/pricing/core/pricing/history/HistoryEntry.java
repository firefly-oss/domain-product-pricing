/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.domain.product.pricing.core.pricing.history;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Single entry in a pricing entity's audit timeline.")
public record HistoryEntry(
        @Schema(description = "When the action happened.") LocalDateTime timestamp,
        @Schema(description = "Action taken: CREATED, UPDATED or DELETED.") String action,
        @Schema(description = "Actor identifier. TODO: wire to centralized audit store.") String actor,
        @Schema(description = "Serialized snapshot of the entity at this point in time.") String snapshot
) {}
