package jcn.yourorderseller.core.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @PrePersist
    void prePersist() {
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
    }

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void reserve(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Reserve amount must be positive");
        }

        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException("Not enough stock");
        }

        this.reservedQuantity += amount;
    }

    public void release(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Release amount must be positive");
        }

        if (reservedQuantity < amount) {
            throw new IllegalStateException("Cannot release more than reserved");
        }

        this.reservedQuantity -= amount;
    }

    public void consumeReserved(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Consume amount must be positive");
        }

        if (reservedQuantity < amount) {
            throw new IllegalStateException("Cannot consume more than reserved");
        }

        if (quantity < amount) {
            throw new IllegalStateException("Cannot consume more than stock quantity");
        }

        this.reservedQuantity -= amount;
        this.quantity -= amount;
    }
}
