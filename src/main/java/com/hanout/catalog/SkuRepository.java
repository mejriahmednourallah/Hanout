package com.hanout.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    Optional<Sku> findBySkuCode(String skuCode);

    List<Sku> findByProductId(Long productId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Sku s SET s.reserved = s.reserved + :qty WHERE s.id = :id AND (s.stockOnHand - s.reserved) >= :qty")
    int reserveStockAtomic(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Sku s SET s.reserved = s.reserved - :qty WHERE s.id = :id AND s.reserved >= :qty")
    int releaseStockAtomic(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Sku s SET s.stockOnHand = s.stockOnHand - :qty, s.reserved = s.reserved - :qty WHERE s.id = :id AND s.stockOnHand >= :qty AND s.reserved >= :qty")
    int commitStockAtomic(@Param("id") Long id, @Param("qty") int qty);
}
