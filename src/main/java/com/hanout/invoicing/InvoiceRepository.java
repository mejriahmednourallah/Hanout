package com.hanout.invoicing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByVendorOrderId(Long vendorOrderId);

    @Query("SELECT COALESCE(MAX(i.sequenceNumber), 0) FROM Invoice i WHERE i.vendorId = :vendorId AND i.fiscalYear = :year")
    Long findMaxSequenceNumber(@Param("vendorId") Long vendorId, @Param("year") int year);
}
