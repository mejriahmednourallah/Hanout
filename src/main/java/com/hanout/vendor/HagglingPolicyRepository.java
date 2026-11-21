package com.hanout.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HagglingPolicyRepository extends JpaRepository<HagglingPolicy, Long> {
    Optional<HagglingPolicy> findByVendorIdAndProductId(Long vendorId, Long productId);
    Optional<HagglingPolicy> findByVendorIdAndProductIdIsNull(Long vendorId);
}
