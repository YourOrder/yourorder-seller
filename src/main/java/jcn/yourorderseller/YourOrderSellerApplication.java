package jcn.yourorderseller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class YourOrderSellerApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourOrderSellerApplication.class, args);
    }
}
