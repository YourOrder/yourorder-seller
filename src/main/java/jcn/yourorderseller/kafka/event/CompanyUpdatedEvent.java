package jcn.yourorderseller.kafka.event;

import java.util.UUID;

public record CompanyUpdatedEvent(
        UUID companyId,
        UUID ownerId
) {
}
