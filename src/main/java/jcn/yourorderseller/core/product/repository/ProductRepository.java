package jcn.yourorderseller.core.product.repository;

import jcn.yourorderseller.core.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByCompanyId(UUID companyId);

    List<Product> findAllByCompanyIdIn(List<UUID> companyIds);

    Page<Product> findAllByCompanyId(UUID companyId, Pageable pageable);

    Page<Product> findAllByCompanyIdIn(List<UUID> companyIds, Pageable pageable);
}
