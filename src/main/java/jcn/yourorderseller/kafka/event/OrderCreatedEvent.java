package jcn.yourorderseller.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        UUID companyId,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderItemEvent> items
) {
}
