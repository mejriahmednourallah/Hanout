package com.hanout.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    Optional<StockReservation> findByRefIdAndStatus(String refId, String status);

    List<StockReservation> findByRefId(String refId);

    @Query("SELECT r FROM StockReservation r WHERE r.status = 'ACTIVE' AND r.expiresAt <= :now")
    List<StockReservation> findExpiredReservations(@Param("now") LocalDateTime now);
}
