package com.hanout.invoicing;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Long vendorOrderId;

    @Column(nullable = false, unique = true, length = 64)
    private String invoiceNumber; // gapless formatted e.g. INV-V1-2026-00001

    @Column(nullable = false)
    private Long sequenceNumber;

    @Column(name = "fiscal_year", nullable = false)
    private int fiscalYear;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String customerAddress;

    private String customerTaxId;

    @Column(nullable = false)
    private String supplierTaxId;

    @Column(nullable = false)
    private Money subtotalHt;

    @Column(nullable = false)
    private Money vatAmount;

    @Column(nullable = false)
    private Money fiscalStamp;

    @Column(nullable = false)
    private Money totalTtc;

    @Column(nullable = false, length = 32)
    private String status = "ISSUED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Invoice() {}

    public Invoice(Long vendorId, Long vendorOrderId, String invoiceNumber, Long sequenceNumber, int fiscalYear,
                   String customerName, String customerAddress, String customerTaxId, String supplierTaxId,
                   Money subtotalHt, Money vatAmount, Money fiscalStamp, Money totalTtc) {
        this.vendorId = vendorId;
        this.vendorOrderId = vendorOrderId;
        this.invoiceNumber = invoiceNumber;
        this.sequenceNumber = sequenceNumber;
        this.fiscalYear = fiscalYear;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerTaxId = customerTaxId;
        this.supplierTaxId = supplierTaxId;
        this.subtotalHt = subtotalHt;
        this.vatAmount = vatAmount;
        this.fiscalStamp = fiscalStamp;
        this.totalTtc = totalTtc;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Long getVendorOrderId() { return vendorOrderId; }
    public void setVendorOrderId(Long vendorOrderId) { this.vendorOrderId = vendorOrderId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerTaxId() { return customerTaxId; }
    public void setCustomerTaxId(String customerTaxId) { this.customerTaxId = customerTaxId; }

    public String getSupplierTaxId() { return supplierTaxId; }
    public void setSupplierTaxId(String supplierTaxId) { this.supplierTaxId = supplierTaxId; }

    public Money getSubtotalHt() { return subtotalHt; }
    public void setSubtotalHt(Money subtotalHt) { this.subtotalHt = subtotalHt; }

    public Money getVatAmount() { return vatAmount; }
    public void setVatAmount(Money vatAmount) { this.vatAmount = vatAmount; }

    public Money getFiscalStamp() { return fiscalStamp; }
    public void setFiscalStamp(Money fiscalStamp) { this.fiscalStamp = fiscalStamp; }

    public Money getTotalTtc() { return totalTtc; }
    public void setTotalTtc(Money totalTtc) { this.totalTtc = totalTtc; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
