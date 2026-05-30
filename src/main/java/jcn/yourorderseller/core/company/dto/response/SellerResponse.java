package jcn.yourorderseller.core.company.dto.response;

import jcn.yourorderseller.core.company.entity.SellerRole;

import java.util.UUID;

public record SellerResponse(
        UUID id,
        UUID userId,
        UUID companyId,
        SellerRole role
) {
}
