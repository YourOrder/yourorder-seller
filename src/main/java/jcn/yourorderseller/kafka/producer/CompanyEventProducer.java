package jcn.yourorderseller.kafka.producer;

import jcn.yourorderseller.core.company.entity.Company;
import jcn.yourorderseller.kafka.config.KafkaTopicsProperties;
import jcn.yourorderseller.kafka.event.CompanyCreatedEvent;
import jcn.yourorderseller.kafka.event.CompanyUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void sendCompanyCreated(Company company) {
        CompanyCreatedEvent event = new CompanyCreatedEvent(company.getId(), company.getOwnerId());
        kafkaTemplate.send(topics.getCompanyCreated(), company.getId().toString(), event);
        log.info("Sent company.created event: {}", event);
    }

    public void sendCompanyUpdated(Company company) {
        CompanyUpdatedEvent event = new CompanyUpdatedEvent(company.getId(), company.getOwnerId());
        kafkaTemplate.send(topics.getCompanyUpdated(), company.getId().toString(), event);
        log.info("Sent company.updated event: {}", event);
    }
}
