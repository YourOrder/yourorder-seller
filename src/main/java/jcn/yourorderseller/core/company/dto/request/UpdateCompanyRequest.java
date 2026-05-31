package jcn.yourorderseller.core.company.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateCompanyRequest(
        @NotBlank
        String name
) {
}
