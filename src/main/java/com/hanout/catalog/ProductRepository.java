package com.hanout.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    List<Product> findByVendorId(Long vendorId);
    List<Product> findByCategoryId(Long categoryId);
}
