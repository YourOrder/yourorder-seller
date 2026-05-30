package jcn.yourorderseller.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID userId,
        LocalDateTime cancelledAt
) {
}
