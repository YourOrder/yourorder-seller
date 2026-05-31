package jcn.yourorderseller.core.product.service;

import jcn.yourorderseller.core.company.service.CompanyService;
import jcn.yourorderseller.core.product.dto.request.CreateProductRequest;
import jcn.yourorderseller.core.product.dto.request.UpdateProductRequest;
import jcn.yourorderseller.core.product.dto.response.ProductResponse;
import jcn.yourorderseller.core.product.entity.Product;
import jcn.yourorderseller.core.product.repository.ProductRepository;
import jcn.yourorderseller.core.stock.entity.Stock;
import jcn.yourorderseller.core.stock.service.StockService;
import jcn.yourorderseller.exception.NotFoundException;
import jcn.yourorderseller.kafka.producer.ProductEventProducer;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final StockService stockService;
    private final CompanyService companyService;
    private final ProductEventProducer productEventProducer;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UUID userId) {
        checkUserCanManageCompany(userId, request.companyId());

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .companyId(request.companyId())
                .build();

        Product savedProduct = productRepository.save(product);
        Stock stock = stockService.createInitialStock(savedProduct.getId(), request.quantity());

        productEventProducer.sendProductCreated(savedProduct, stock);
        return toResponse(savedProduct, stock);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(UUID companyId, UserPrincipal user, Pageable pageable) {
        if (isAdmin(user)) {
            if (companyId == null) {
                return productRepository.findAll(pageable).map(this::toResponse);
            }
            return productRepository.findAllByCompanyId(companyId, pageable).map(this::toResponse);
        }

        if (companyId == null) {
            return productRepository.findAllByCompanyIdIn(companyService.getMyCompanyIds(user.userId()), pageable)
                    .map(this::toResponse);
        }

        checkUserCanManageCompany(user.userId(), companyId);
        return productRepository.findAllByCompanyId(companyId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }

        product.setName(request.name());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());

        Product savedProduct = productRepository.save(product);
        Stock stock = stockService.getStock(savedProduct.getId());

        productEventProducer.sendProductUpdated(savedProduct, stock);
        return toResponse(savedProduct, stock);
    }

    @Transactional
    public ProductResponse updateStockQuantity(UUID productId, Integer quantity, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }

        Stock stock = stockService.updateQuantity(productId, quantity);
        productEventProducer.sendProductUpdated(product, stock);
        return toResponse(product, stock);
    }

    @Transactional
    public ProductResponse reserveStock(UUID productId, Integer amount, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }

        Stock stock = stockService.reserve(productId, amount);
        productEventProducer.sendProductUpdated(product, stock);
        return toResponse(product, stock);
    }

    @Transactional
    public ProductResponse releaseStock(UUID productId, Integer amount, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }

        Stock stock = stockService.release(productId, amount);
        productEventProducer.sendProductUpdated(product, stock);
        return toResponse(product, stock);
    }

    @Transactional
    public void deleteProduct(UUID productId, UserPrincipal user) {
        Product product = getProductOrThrow(productId);
        if (!isAdmin(user)) {
            checkUserCanManageCompany(user.userId(), product.getCompanyId());
        }

        productEventProducer.sendProductDeleted(product);
        stockService.deleteByProductId(productId);
        productRepository.delete(product);
    }

    private Product getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private ProductResponse toResponse(Product product) {
        Stock stock = stockService.getStock(product.getId());
        return toResponse(product, stock);
    }

    private ProductResponse toResponse(Product product, Stock stock) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCompanyId(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                stock.getQuantity(),
                stock.getReservedQuantity()
        );
    }

    private void checkUserCanManageCompany(UUID userId, UUID companyId) {
        if (!companyService.isSellerInCompany(userId, companyId)) {
            throw new IllegalStateException("User is not seller in this company");
        }
    }

    private boolean isAdmin(UserPrincipal user) {
        return "ADMIN".equals(user.role());
    }
}
