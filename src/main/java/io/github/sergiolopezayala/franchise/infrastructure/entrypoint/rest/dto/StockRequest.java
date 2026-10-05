package io.github.sergiolopezayala.franchise.infrastructure.entrypoint.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequest(@NotNull @PositiveOrZero Integer stock) {
}
