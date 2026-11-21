package com.hanout.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductI18nRepository extends JpaRepository<ProductI18n, Long> {
    List<ProductI18n> findByProductId(Long productId);
    Optional<ProductI18n> findByProductIdAndLocale(Long productId, String locale);
}
