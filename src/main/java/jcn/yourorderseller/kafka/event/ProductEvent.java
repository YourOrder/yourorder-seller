package jcn.yourorderseller.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductEvent(
        UUID id,
        String name,
        BigDecimal price,
        UUID companyId
) {
}
