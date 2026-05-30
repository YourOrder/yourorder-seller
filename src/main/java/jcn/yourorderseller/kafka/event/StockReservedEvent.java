package jcn.yourorderseller.kafka.event;

import java.util.UUID;

public record StockReservedEvent(
        UUID orderId
) {
}
