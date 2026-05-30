package jcn.yourorderseller.core.stock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockQuantityRequest(
        @NotNull
        @Min(0)
        Integer quantity
) {
}
