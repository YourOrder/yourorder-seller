package jcn.yourorderseller.core.company.repository;

import jcn.yourorderseller.core.company.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {

    boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId);

    Optional<Seller> findByUserIdAndCompanyId(UUID userId, UUID companyId);

    List<Seller> findAllByUserId(UUID userId);

    List<Seller> findAllByCompanyId(UUID companyId);

    Page<Seller> findAllByUserId(UUID userId, Pageable pageable);

    Page<Seller> findAllByCompanyId(UUID companyId, Pageable pageable);
}
