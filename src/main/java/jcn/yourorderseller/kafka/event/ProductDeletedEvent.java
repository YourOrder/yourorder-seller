package jcn.yourorderseller.kafka.event;

import java.util.UUID;

public record ProductDeletedEvent(
        UUID id,
        UUID companyId
) {
}
