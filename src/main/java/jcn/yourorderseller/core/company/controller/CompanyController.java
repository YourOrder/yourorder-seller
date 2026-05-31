package jcn.yourorderseller.core.company.controller;

import jakarta.validation.Valid;
import jcn.yourorderseller.core.company.dto.request.AddSellerRequest;
import jcn.yourorderseller.core.company.dto.request.CreateCompanyRequest;
import jcn.yourorderseller.core.company.dto.request.UpdateCompanyRequest;
import jcn.yourorderseller.core.company.dto.response.CompanyResponse;
import jcn.yourorderseller.core.company.dto.response.SellerResponse;
import jcn.yourorderseller.core.company.service.CompanyService;
import jcn.yourorderseller.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/seller/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PostMapping
    public CompanyResponse createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return companyService.createCompany(request, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyService.getAllCompanies(pageable);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @GetMapping("/my")
    public Page<CompanyResponse> getMyCompanies(
            @AuthenticationPrincipal UserPrincipal user,
            Pageable pageable
    ) {
        return companyService.getMyCompanies(user.userId(), pageable);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return companyService.getCompany(companyId, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PutMapping("/{companyId}")
    public CompanyResponse updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return companyService.updateCompany(companyId, request, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @DeleteMapping("/{companyId}")
    public void deleteCompany(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        companyService.deleteCompany(companyId, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PostMapping("/{companyId}/sellers")
    public SellerResponse addSeller(
            @PathVariable UUID companyId,
            @Valid @RequestBody AddSellerRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return companyService.addSeller(companyId, request, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @GetMapping("/{companyId}/sellers")
    public Page<SellerResponse> getCompanySellers(
            @PathVariable UUID companyId,
            @AuthenticationPrincipal UserPrincipal user,
            Pageable pageable
    ) {
        return companyService.getCompanySellers(companyId, user, pageable);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @DeleteMapping("/{companyId}/sellers/{sellerUserId}")
    public void removeSeller(
            @PathVariable UUID companyId,
            @PathVariable UUID sellerUserId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        companyService.removeSeller(companyId, sellerUserId, user);
    }
}
