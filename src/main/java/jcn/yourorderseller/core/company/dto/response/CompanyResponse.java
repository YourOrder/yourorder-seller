package jcn.yourorderseller.core.company.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        UUID ownerId,
        Instant createdAt
) {
}
