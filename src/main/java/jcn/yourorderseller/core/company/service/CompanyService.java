package jcn.yourorderseller.core.company.service;

import jcn.yourorderseller.core.company.dto.request.AddSellerRequest;
import jcn.yourorderseller.core.company.dto.request.CreateCompanyRequest;
import jcn.yourorderseller.core.company.dto.response.CompanyResponse;
import jcn.yourorderseller.core.company.dto.response.SellerResponse;
import jcn.yourorderseller.core.company.entity.Company;
import jcn.yourorderseller.core.company.entity.Seller;
import jcn.yourorderseller.core.company.entity.SellerRole;
import jcn.yourorderseller.core.company.repository.CompanyRepository;
import jcn.yourorderseller.core.company.repository.SellerRepository;
import jcn.yourorderseller.exception.NotFoundException;
import jcn.yourorderseller.kafka.producer.CompanyEventProducer;
import jcn.yourorderseller.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SellerRepository sellerRepository;
    private final CompanyEventProducer companyEventProducer;

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request, UserPrincipal user) {
        UUID ownerId = isAdmin(user) && request.ownerId() != null
                ? request.ownerId()
                : user.userId();

        Company company = Company.builder()
                .name(request.name())
                .ownerId(ownerId)
                .build();

        Company savedCompany = companyRepository.save(company);

        Seller owner = Seller.builder()
                .userId(ownerId)
                .companyId(savedCompany.getId())
                .role(SellerRole.OWNER)
                .build();

        sellerRepository.save(owner);

        companyEventProducer.sendCompanyCreated(savedCompany);

        return toCompanyResponse(savedCompany);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::toCompanyResponse);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getMyCompanies(UUID userId, Pageable pageable) {
        return sellerRepository.findAllByUserId(userId, pageable)
                .map(Seller::getCompanyId)
                .map(this::getCompanyByIdOrThrow)
                .map(this::toCompanyResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(UUID companyId, UserPrincipal user) {
        if (!isAdmin(user)) {
            checkUserInCompany(user.userId(), companyId);
        }

        Company company = getCompanyByIdOrThrow(companyId);

        return toCompanyResponse(company);
    }

    @Transactional
    public SellerResponse addSeller(UUID companyId, AddSellerRequest request, UserPrincipal user) {
        if (!isAdmin(user)) {
            checkUserIsOwner(user.userId(), companyId);
        }

        if (sellerRepository.existsByUserIdAndCompanyId(request.userId(), companyId)) {
            throw new IllegalStateException("User is already seller in this company");
        }

        Seller seller = Seller.builder()
                .userId(request.userId())
                .companyId(companyId)
                .role(SellerRole.EMPLOYEE)
                .build();

        Seller savedSeller = sellerRepository.save(seller);

        companyEventProducer.sendCompanyUpdated(getCompanyByIdOrThrow(companyId));

        return toSellerResponse(savedSeller);
    }

    @Transactional(readOnly = true)
    public Page<SellerResponse> getCompanySellers(UUID companyId, UserPrincipal user, Pageable pageable) {
        if (!isAdmin(user)) {
            checkUserInCompany(user.userId(), companyId);
        }

        return sellerRepository.findAllByCompanyId(companyId, pageable)
                .map(this::toSellerResponse);
    }

    @Transactional
    public void removeSeller(UUID companyId, UUID sellerUserId, UserPrincipal user) {
        if (!isAdmin(user)) {
            checkUserIsOwner(user.userId(), companyId);
        }

        if (!isAdmin(user) && user.userId().equals(sellerUserId)) {
            throw new IllegalStateException("Owner cannot remove himself");
        }

        Seller seller = sellerRepository.findByUserIdAndCompanyId(sellerUserId, companyId)
                .orElseThrow(() -> new IllegalStateException("Seller not found in this company"));

        sellerRepository.delete(seller);

        companyEventProducer.sendCompanyUpdated(getCompanyByIdOrThrow(companyId));
    }

    @Transactional(readOnly = true)
    public boolean isSellerInCompany(UUID userId, UUID companyId) {
        return sellerRepository.existsByUserIdAndCompanyId(userId, companyId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getMyCompanyIds(UUID userId) {
        return sellerRepository.findAllByUserId(userId)
                .stream()
                .map(Seller::getCompanyId)
                .toList();
    }

    private boolean isAdmin(UserPrincipal user) {
        return "ADMIN".equals(user.role());
    }

    private void checkUserInCompany(UUID userId, UUID companyId) {
        if (!sellerRepository.existsByUserIdAndCompanyId(userId, companyId)) {
            throw new IllegalStateException("User is not seller in this company");
        }
    }

    private void checkUserIsOwner(UUID userId, UUID companyId) {
        Seller seller = sellerRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new IllegalStateException("User is not seller in this company"));

        if (seller.getRole() != SellerRole.OWNER) {
            throw new IllegalStateException("Only company owner can do this action");
        }
    }

    private Company getCompanyByIdOrThrow(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
    }

    private CompanyResponse toCompanyResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getOwnerId(),
                company.getCreatedAt()
        );
    }

    private SellerResponse toSellerResponse(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getUserId(),
                seller.getCompanyId(),
                seller.getRole()
        );
    }
}
