package jcn.yourorderseller.core.stock.service;

import jcn.yourorderseller.core.stock.dto.response.StockResponse;
import jcn.yourorderseller.core.stock.entity.Stock;
import jcn.yourorderseller.core.stock.entity.StockReservation;
import jcn.yourorderseller.core.stock.repository.StockReservationRepository;
import jcn.yourorderseller.core.stock.repository.StockRepository;
import jcn.yourorderseller.exception.NotFoundException;
import jcn.yourorderseller.kafka.event.OrderCreatedEvent;
import jcn.yourorderseller.kafka.event.OrderItemEvent;
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
    public void reserveOrder(OrderCreatedEvent event) {
        if (stockReservationRepository.existsByOrderId(event.orderId())) {
            return;
        }

        for (OrderItemEvent item : event.items()) {
            reserve(item.productId(), item.quantity());
            stockReservationRepository.save(StockReservation.builder()
                    .orderId(event.orderId())
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .build());
        }

        stockEventProducer.sendStockReserved(event.orderId());
    }

    @Transactional
    public void releaseOrder(UUID orderId) {
        List<StockReservation> reservations = stockReservationRepository.findAllByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            release(reservation.getProductId(), reservation.getQuantity());
        }

        stockReservationRepository.deleteAllByOrderId(orderId);

        if (!reservations.isEmpty()) {
            stockEventProducer.sendStockReleased(orderId);
        }
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
}
