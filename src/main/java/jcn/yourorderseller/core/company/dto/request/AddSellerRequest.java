package jcn.yourorderseller.core.company.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddSellerRequest(
        @NotNull
        UUID userId
) {
}