package com.hanout.invoicing;

import com.hanout.common.Money;
import com.hanout.ordering.OrderPlacedEvent;
import com.hanout.vendor.Vendor;
import com.hanout.vendor.VendorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class InvoicingService {

    private static final Logger log = LoggerFactory.getLogger(InvoicingService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository lineRepository;
    private final VendorService vendorService;
    private final InvoiceExporter invoiceExporter;

    @Value("${hanout.fiscal-stamp-millimes:1000}")
    private long fiscalStampMillimes;

    public InvoicingService(InvoiceRepository invoiceRepository,
                            InvoiceLineRepository lineRepository,
                            VendorService vendorService,
                            InvoiceExporter invoiceExporter) {
        this.invoiceRepository = invoiceRepository;
        this.lineRepository = lineRepository;
        this.vendorService = vendorService;
        this.invoiceExporter = invoiceExporter;
    }

    @ApplicationModuleListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Generating invoices for Order #{}", event.orderId());
        for (Long vendorOrderId : event.vendorOrderIds()) {
            try {
                generateInvoiceForSubOrder(event, vendorOrderId);
            } catch (Exception e) {
                log.error("Failed to generate invoice for vendor order {}", vendorOrderId, e);
            }
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Invoice generateInvoiceForSubOrder(OrderPlacedEvent event, Long vendorOrderId) {
        // Idempotency check: don't duplicate invoice
        Optional<Invoice> existing = invoiceRepository.findByVendorOrderId(vendorOrderId);
        if (existing.isPresent()) {
            return existing.get();
        }

        int currentYear = LocalDate.now().getYear();
        Long vendorId = 1L; // default or resolved from subOrder
        Vendor vendor = vendorService.findById(vendorId).orElse(null);

        boolean isVatRegistered = vendor != null && vendor.isVatRegistered();
        String supplierTaxId = vendor != null && vendor.getTaxId() != null ? vendor.getTaxId() : "0000000/A/M/000";

        // Gapless sequential numbering per vendor per year
        long nextSeq = invoiceRepository.findMaxSequenceNumber(vendorId, currentYear) + 1;
        String invoiceNumber = String.format("INV-V%d-%d-%05d", vendorId, currentYear, nextSeq);

        Money stamp = Money.ofMillimes(fiscalStampMillimes);
        BigDecimal effectiveVatRate = isVatRegistered ? BigDecimal.valueOf(19.0) : BigDecimal.ZERO;

        Money subtotalTtc = event.totalAmount();
        Money subtotalHt = isVatRegistered ? subtotalTtc.deriveHt(effectiveVatRate) : subtotalTtc;
        Money vatAmount = subtotalTtc.minus(subtotalHt);
        Money grandTotalTtc = subtotalTtc.plus(stamp);

        Invoice invoice = new Invoice(
                vendorId,
                vendorOrderId,
                invoiceNumber,
                nextSeq,
                currentYear,
                "Customer Phone: " + event.phone(),
                event.shippingAddress(),
                null,
                supplierTaxId,
                subtotalHt,
                vatAmount,
                stamp,
                grandTotalTtc
        );
        invoice = invoiceRepository.save(invoice);

        InvoiceLine line = new InvoiceLine(
                invoice.getId(),
                "Marketplace order items for Vendor Order #" + vendorOrderId,
                1,
                subtotalHt,
                effectiveVatRate,
                vatAmount,
                subtotalTtc
        );
        lineRepository.save(line);

        log.info("Issued invoice {} for Vendor Order #{}", invoice.getInvoiceNumber(), vendorOrderId);
        return invoice;
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public Optional<Invoice> findByVendorOrderId(Long vendorOrderId) {
        return invoiceRepository.findByVendorOrderId(vendorOrderId);
    }

    public byte[] exportInvoice(Long invoiceId, String format) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        List<InvoiceLine> lines = lineRepository.findByInvoiceId(invoiceId);
        return invoiceExporter.export(invoice, lines, format);
    }
}
