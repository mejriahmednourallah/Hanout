package com.hanout.haggling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @Query("SELECT o FROM Offer o WHERE o.buyerId = :buyerId AND o.skuId = :skuId AND o.status IN ('PENDING', 'COUNTERED', 'ACCEPTED')")
    Optional<Offer> findActiveOfferByBuyerAndSku(@Param("buyerId") Long buyerId, @Param("skuId") Long skuId);

    List<Offer> findByVendorIdAndStatus(Long vendorId, OfferStatus status);

    List<Offer> findByBuyerId(Long buyerId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Offer o SET o.status = :newStatus, o.version = o.version + 1, o.updatedAt = :now WHERE o.id = :id AND o.status = :expectedStatus AND o.version = :expectedVersion")
    int updateStatusGuarded(@Param("id") Long id,
                            @Param("expectedStatus") OfferStatus expectedStatus,
                            @Param("newStatus") OfferStatus newStatus,
                            @Param("expectedVersion") Long expectedVersion,
                            @Param("now") LocalDateTime now);

    @Query("SELECT o FROM Offer o WHERE o.status IN ('PENDING', 'COUNTERED') AND o.expiresAt <= :now")
    List<Offer> findExpiredPendingOffers(@Param("now") LocalDateTime now);

    @Query("SELECT o FROM Offer o WHERE o.status = 'ACCEPTED' AND o.lockExpiresAt IS NOT NULL AND o.lockExpiresAt <= :now")
    List<Offer> findExpiredLockedOffers(@Param("now") LocalDateTime now);
}
