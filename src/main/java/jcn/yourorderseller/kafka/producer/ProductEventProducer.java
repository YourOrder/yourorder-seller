package jcn.yourorderseller.kafka.producer;

import jcn.yourorderseller.core.product.entity.Product;
import jcn.yourorderseller.kafka.config.KafkaTopicsProperties;
import jcn.yourorderseller.kafka.event.ProductDeletedEvent;
import jcn.yourorderseller.kafka.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void sendProductCreated(Product product) {
        ProductEvent event = toEvent(product);
        kafkaTemplate.send(topics.getProductCreated(), product.getId().toString(), event);
        log.info("Sent product.created event: {}", event);
    }

    public void sendProductUpdated(Product product) {
        ProductEvent event = toEvent(product);
        kafkaTemplate.send(topics.getProductUpdated(), product.getId().toString(), event);
        log.info("Sent product.updated event: {}", event);
    }

    public void sendProductDeleted(Product product) {
        ProductDeletedEvent event = new ProductDeletedEvent(product.getId(), product.getCompanyId());
        kafkaTemplate.send(topics.getProductDeleted(), product.getId().toString(), event);
        log.info("Sent product.deleted event: {}", event);
    }

    private ProductEvent toEvent(Product product) {
        return new ProductEvent(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCompanyId()
        );
    }
}
