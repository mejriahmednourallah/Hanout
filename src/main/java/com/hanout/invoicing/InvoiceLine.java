package com.hanout.invoicing;

import com.hanout.common.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int qty;

    @Column(name = "unit_price_ht", nullable = false)
    private Money unitPriceHt;

    @Column(nullable = false)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", nullable = false)
    private Money vatAmount;

    @Column(name = "total_ttc", nullable = false)
    private Money totalTtc;

    public InvoiceLine() {}

    public InvoiceLine(Long invoiceId, String description, int qty, Money unitPriceHt, BigDecimal vatRate, Money vatAmount, Money totalTtc) {
        this.invoiceId = invoiceId;
        this.description = description;
        this.qty = qty;
        this.unitPriceHt = unitPriceHt;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.totalTtc = totalTtc;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public Money getUnitPriceHt() { return unitPriceHt; }
    public void setUnitPriceHt(Money unitPriceHt) { this.unitPriceHt = unitPriceHt; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public Money getVatAmount() { return vatAmount; }
    public void setVatAmount(Money vatAmount) { this.vatAmount = vatAmount; }

    public Money getTotalTtc() { return totalTtc; }
    public void setTotalTtc(Money totalTtc) { this.totalTtc = totalTtc; }
}
