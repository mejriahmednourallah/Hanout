package com.hanout.vendor;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final HagglingPolicyRepository policyRepository;

    public VendorService(VendorRepository vendorRepository, HagglingPolicyRepository policyRepository) {
        this.vendorRepository = vendorRepository;
        this.policyRepository = policyRepository;
    }

    public Optional<Vendor> findById(Long id) {
        return vendorRepository.findById(id);
    }

    public Optional<Vendor> findByUserId(Long userId) {
        return vendorRepository.findByUserId(userId);
    }

    public HagglingPolicy getEffectivePolicy(Long vendorId, Long productId) {
        if (productId != null) {
            Optional<HagglingPolicy> productPolicy = policyRepository.findByVendorIdAndProductId(vendorId, productId);
            if (productPolicy.isPresent()) {
                return productPolicy.get();
            }
        }
        return policyRepository.findByVendorIdAndProductIdIsNull(vendorId)
                .orElseGet(() -> new HagglingPolicy(
                        vendorId,
                        null,
                        java.math.BigDecimal.valueOf(95.0),
                        java.math.BigDecimal.valueOf(70.0),
                        3,
                        24,
                        30,
                        com.hanout.common.Money.ZERO
                ));
    }

    public HagglingPolicy savePolicy(HagglingPolicy policy) {
        return policyRepository.save(policy);
    }
}
