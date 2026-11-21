package com.hanout.haggling;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferEventRepository extends JpaRepository<OfferEvent, Long> {
    List<OfferEvent> findByOfferIdOrderByCreatedAtAsc(Long offerId);
}
