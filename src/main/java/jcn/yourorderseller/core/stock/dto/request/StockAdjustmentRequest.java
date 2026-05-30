package jcn.yourorderseller.core.stock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull
        @Min(1)
        Integer amount
) {
}
