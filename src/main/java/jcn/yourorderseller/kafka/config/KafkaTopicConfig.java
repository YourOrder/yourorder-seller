package jcn.yourorderseller.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicsProperties topics;

    @Bean
    public NewTopic companyCreatedTopic() {
        return new NewTopic(topics.getCompanyCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic companyUpdatedTopic() {
        return new NewTopic(topics.getCompanyUpdated(), 1, (short) 1);
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return new NewTopic(topics.getProductCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return new NewTopic(topics.getProductUpdated(), 1, (short) 1);
    }

    @Bean
    public NewTopic productDeletedTopic() {
        return new NewTopic(topics.getProductDeleted(), 1, (short) 1);
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic(topics.getOrderCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return new NewTopic(topics.getOrderCancelled(), 1, (short) 1);
    }

    @Bean
    public NewTopic stockReservedTopic() {
        return new NewTopic(topics.getStockReserved(), 1, (short) 1);
    }

    @Bean
    public NewTopic stockReleasedTopic() {
        return new NewTopic(topics.getStockReleased(), 1, (short) 1);
    }
}
