package com.hanout.ordering;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorOrderRepository extends JpaRepository<VendorOrder, Long> {
    List<VendorOrder> findByOrderId(Long orderId);
    List<VendorOrder> findByVendorId(Long vendorId);
}
