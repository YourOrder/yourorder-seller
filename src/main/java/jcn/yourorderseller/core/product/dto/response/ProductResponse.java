package jcn.yourorderseller.core.product.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        UUID companyId,
        Instant createdAt,
        Instant updatedAt,
        Integer quantity,
        Integer reservedQuantity
) {
}
