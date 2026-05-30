package jcn.yourorderseller.security.model;

import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String role,
        UUID tenantId
) {
}
