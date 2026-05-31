package jcn.yourorderseller.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jcn.yourorderseller.core.stock.service.StockService;
import jcn.yourorderseller.kafka.event.OrderCancelledEvent;
import jcn.yourorderseller.kafka.event.OrderCreatedEvent;
import jcn.yourorderseller.kafka.event.PaymentCompletedEvent;
import jcn.yourorderseller.kafka.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final StockService stockService;

    @PostConstruct
    void registerJavaTimeModule() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.orderCreated}", groupId = "yourorder-seller")
    public void handleOrderCreated(String message) throws Exception {
        OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
        log.info("Received order.created event: {}", event);
        stockService.reserveOrder(event);
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.orderCancelled}", groupId = "yourorder-seller")
    public void handleOrderCancelled(String message) throws Exception {
        OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
        log.info("Received order.cancelled event: {}", event);
        stockService.releaseOrder(event.orderId());
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentCompleted}", groupId = "yourorder-seller")
    public void handlePaymentCompleted(String message) throws Exception {
        PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);
        log.info("Received payment.completed event: {}", event);
        stockService.confirmOrder(event.orderId());
    }

    @KafkaListener(topics = "#{@kafkaTopicsProperties.paymentFailed}", groupId = "yourorder-seller")
    public void handlePaymentFailed(String message) throws Exception {
        PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
        log.info("Received payment.failed event: {}", event);
        stockService.releaseOrder(event.orderId());
    }
}
