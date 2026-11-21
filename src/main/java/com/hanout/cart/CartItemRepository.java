package com.hanout.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByBuyerId(Long buyerId);
    Optional<CartItem> findByBuyerIdAndSkuId(Long buyerId, Long skuId);
    void deleteByBuyerId(Long buyerId);
}
