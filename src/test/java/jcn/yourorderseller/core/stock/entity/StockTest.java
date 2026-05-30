package jcn.yourorderseller.core.stock.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    void reserveAndReleaseChangeAvailableQuantity() {
        Stock stock = Stock.builder()
                .productId(UUID.randomUUID())
                .quantity(10)
                .reservedQuantity(0)
                .build();

        stock.reserve(3);
        stock.release(1);

        assertThat(stock.getReservedQuantity()).isEqualTo(2);
        assertThat(stock.getAvailableQuantity()).isEqualTo(8);
    }

    @Test
    void cannotReserveMoreThanAvailableQuantity() {
        Stock stock = Stock.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .reservedQuantity(0)
                .build();

        assertThatThrownBy(() -> stock.reserve(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough stock");
    }
}
