package com.hanout.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByVendorOrderId(Long vendorOrderId);
    Optional<Shipment> findByTrackingNo(String trackingNo);
}
