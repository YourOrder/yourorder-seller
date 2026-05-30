package jcn.yourorderseller.core.product.controller;

import jakarta.validation.Valid;
import jcn.yourorderseller.core.product.dto.request.CreateProductRequest;
import jcn.yourorderseller.core.product.dto.request.UpdateProductRequest;
import jcn.yourorderseller.core.product.dto.response.ProductResponse;
import jcn.yourorderseller.core.product.service.ProductService;
import jcn.yourorderseller.core.stock.dto.request.StockAdjustmentRequest;
import jcn.yourorderseller.core.stock.dto.request.UpdateStockQuantityRequest;
import jcn.yourorderseller.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.createProduct(request, user.userId());
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @GetMapping
    public Page<ProductResponse> getProducts(
            @RequestParam(required = false) UUID companyId,
            @AuthenticationPrincipal UserPrincipal user,
            Pageable pageable
    ) {
        return productService.getProducts(companyId, user, pageable);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @GetMapping("/{productId}")
    public ProductResponse getProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.getProduct(productId, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.updateProduct(productId, request, user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PutMapping("/{productId}/stock")
    public ProductResponse updateStockQuantity(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateStockQuantityRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.updateStockQuantity(productId, request.quantity(), user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PatchMapping("/{productId}/stock/reserve")
    public ProductResponse reserveStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.reserveStock(productId, request.amount(), user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @PatchMapping("/{productId}/stock/release")
    public ProductResponse releaseStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return productService.releaseStock(productId, request.amount(), user);
    }

    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        productService.deleteProduct(productId, user);
    }
}
