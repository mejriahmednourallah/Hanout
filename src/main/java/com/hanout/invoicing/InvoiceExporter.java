package com.hanout.invoicing;

import java.util.List;

public interface InvoiceExporter {
    byte[] export(Invoice invoice, List<InvoiceLine> lines, String format);
}
