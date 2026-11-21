package com.hanout.invoicing;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DefaultInvoiceExporter implements InvoiceExporter {

    @Override
    public byte[] export(Invoice invoice, List<InvoiceLine> lines, String format) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FACTURE ELECTRONIQUE / INVOICE ===\n");
        sb.append("Invoice Number: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("Supplier Tax ID: ").append(invoice.getSupplierTaxId()).append("\n");
        sb.append("Customer: ").append(invoice.getCustomerName()).append(" | ").append(invoice.getCustomerAddress()).append("\n");
        sb.append("---------------------------------------\n");
        for (InvoiceLine l : lines) {
            sb.append(l.getDescription())
                    .append(" x").append(l.getQty())
                    .append(" | HT: ").append(l.getUnitPriceHt())
                    .append(" | TVA: ").append(l.getVatRate()).append("% (").append(l.getVatAmount()).append(")")
                    .append(" | TTC: ").append(l.getTotalTtc()).append("\n");
        }
        sb.append("---------------------------------------\n");
        sb.append("Total HT: ").append(invoice.getSubtotalHt()).append(" TND\n");
        sb.append("Total TVA: ").append(invoice.getVatAmount()).append(" TND\n");
        sb.append("Timbre Fiscal (Fiscal Stamp): ").append(invoice.getFiscalStamp()).append(" TND\n");
        sb.append("Total TTC a Payer: ").append(invoice.getTotalTtc()).append(" TND\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
