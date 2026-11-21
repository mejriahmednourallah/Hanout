package com.hanout.invoicing;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoicingService invoicingService;

    public InvoiceController(InvoicingService invoicingService) {
        this.invoicingService = invoicingService;
    }

    @GetMapping("/by-order/{subOrderId}")
    public ResponseEntity<Invoice> getBySubOrder(@PathVariable Long subOrderId) {
        return invoicingService.findByVendorOrderId(subOrderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportInvoice(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        byte[] doc = invoicingService.exportInvoice(id, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(doc);
    }
}
