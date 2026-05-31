package jcn.yourorderseller.core.stock.service;

import jcn.yourorderseller.core.stock.dto.response.StockResponse;
import jcn.yourorderseller.core.stock.entity.Stock;
import jcn.yourorderseller.core.stock.entity.StockReservation;
import jcn.yourorderseller.core.product.entity.Product;
import jcn.yourorderseller.core.product.repository.ProductRepository;
import jcn.yourorderseller.core.stock.repository.StockReservationRepository;
import jcn.yourorderseller.core.stock.repository.StockRepository;
import jcn.yourorderseller.exception.NotFoundException;
import jcn.yourorderseller.kafka.event.OrderCreatedEvent;
import jcn.yourorderseller.kafka.event.OrderItemEvent;
import jcn.yourorderseller.kafka.producer.ProductEventProducer;
import jcn.yourorderseller.kafka.producer.StockEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockReservationRepository stockReservationRepository;
    private final StockEventProducer stockEventProducer;
    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    @Transactional
    public Stock createInitialStock(UUID productId, Integer quantity) {
        Stock stock = Stock.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(0)
                .build();

        return stockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public Stock getStock(UUID productId) {
        return stockRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Stock not found"));
    }

    @Transactional
    public Stock reserve(UUID productId, Integer amount) {
        Stock stock = getStock(productId);
        stock.reserve(amount);
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock release(UUID productId, Integer amount) {
        Stock stock = getStock(productId);
        stock.release(amount);
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock updateQuantity(UUID productId, Integer quantity) {
        Stock stock = getStock(productId);
        stock.setQuantity(quantity);
        if (stock.getAvailableQuantity() < 0) {
            throw new IllegalStateException("Quantity cannot be lower than reserved quantity");
        }
        return stockRepository.save(stock);
    }

    @Transactional
    public void reserveOrder(OrderCreatedEvent event) {
        if (stockReservationRepository.existsByOrderId(event.orderId())) {
            return;
        }

        for (OrderItemEvent item : event.items()) {
            Stock stock = reserve(item.productId(), item.quantity());
            stockReservationRepository.save(StockReservation.builder()
                    .orderId(event.orderId())
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .build());
            sendProductStockUpdated(item.productId(), stock);
        }

        stockEventProducer.sendStockReserved(event.orderId());
    }

    @Transactional
    public void releaseOrder(UUID orderId) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            Stock stock = release(reservation.getProductId(), reservation.getQuantity());
            sendProductStockUpdated(reservation.getProductId(), stock);
        }

        stockReservationRepository.deleteAllByOrderId(orderId);

        if (!reservations.isEmpty()) {
            stockEventProducer.sendStockReleased(orderId);
        }
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            Stock stock = getStock(reservation.getProductId());
            stock.consumeReserved(reservation.getQuantity());
            Stock saved = stockRepository.save(stock);
            sendProductStockUpdated(reservation.getProductId(), saved);
        }

        stockReservationRepository.deleteAllByOrderId(orderId);
    }

    @Transactional
    public void deleteByProductId(UUID productId) {
        stockRepository.deleteById(productId);
    }

    public StockResponse toResponse(Stock stock) {
        return new StockResponse(
                stock.getProductId(),
                stock.getQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity()
        );
    }

    private void sendProductStockUpdated(UUID productId, Stock stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        productEventProducer.sendProductUpdated(product, stock);
    }
}
