package jcn.yourorderseller.core.company.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCompanyRequest(
        @NotBlank
        String name,

        UUID ownerId
) {
}
