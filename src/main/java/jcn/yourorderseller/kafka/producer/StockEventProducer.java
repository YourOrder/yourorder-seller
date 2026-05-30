package jcn.yourorderseller.kafka.producer;

import jcn.yourorderseller.kafka.config.KafkaTopicsProperties;
import jcn.yourorderseller.kafka.event.StockReleasedEvent;
import jcn.yourorderseller.kafka.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void sendStockReserved(UUID orderId) {
        StockReservedEvent event = new StockReservedEvent(orderId);
        kafkaTemplate.send(topics.getStockReserved(), orderId.toString(), event);
        log.info("Sent stock.reserved event: {}", event);
    }

    public void sendStockReleased(UUID orderId) {
        StockReleasedEvent event = new StockReleasedEvent(orderId);
        kafkaTemplate.send(topics.getStockReleased(), orderId.toString(), event);
        log.info("Sent stock.released event: {}", event);
    }
}
