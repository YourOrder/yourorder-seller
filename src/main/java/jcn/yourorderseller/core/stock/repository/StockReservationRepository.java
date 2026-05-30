package jcn.yourorderseller.core.stock.repository;

import jcn.yourorderseller.core.stock.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    boolean existsByOrderId(UUID orderId);

    List<StockReservation> findAllByOrderId(UUID orderId);

    void deleteAllByOrderId(UUID orderId);
}
