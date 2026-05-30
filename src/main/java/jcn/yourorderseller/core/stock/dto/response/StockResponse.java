package jcn.yourorderseller.core.stock.dto.response;

import java.util.UUID;

public record StockResponse(
        UUID productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
