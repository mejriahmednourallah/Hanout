package com.hanout.ordering;

import com.hanout.cart.CartService;
import com.hanout.catalog.Sku;
import com.hanout.catalog.SkuRepository;
import com.hanout.common.Money;
import com.hanout.invoicing.Invoice;
import com.hanout.invoicing.InvoicingService;
import com.hanout.payout.LedgerAccount;
import com.hanout.payout.PayoutLedgerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CheckoutSagaIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private CheckoutSagaService checkoutSagaService;

    @Autowired
    private InvoicingService invoicingService;

    @Autowired
    private PayoutLedgerService payoutLedgerService;

    @Autowired
    private VendorOrderRepository vendorOrderRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    @Test
    @DisplayName("End-to-End Checkout Saga: Cart -> Risk -> Split -> Snapshots -> Payment -> Invoicing -> Ledger")
    void testEndToEndCheckout() {
        Long buyerId = 1L;

        // 1. Add Harissa SKU (SKU-HRS-NAB-1KG) to cart (price: 18.500 TND)
        Sku sku = skuRepository.findBySkuCode("SKU-HRS-NAB-1KG").orElseThrow();
        int initialStock = sku.getStockOnHand();

        cartService.clearCart(buyerId);
        cartService.addToCart(buyerId, sku.getId(), 2, null);

        // 2. Execute Checkout Saga with Idempotency Key
        String idempotencyKey = "IDEMP-TEST-" + System.currentTimeMillis();
        CheckoutSagaService.CheckoutCommand cmd = new CheckoutSagaService.CheckoutCommand(
                buyerId,
                "+21620111222",
                "Avenue Habib Bourguiba, Tunis",
                1L,
                "COD",
                idempotencyKey
        );

        CheckoutSagaService.CheckoutResponse response = checkoutSagaService.execute(cmd);

        // Assert Order Created
        assertThat(response.orderId()).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(response.totalAmount()).isEqualTo(Money.ofMillimes(37000)); // 2 * 18.500 = 37.000 TND
        assertThat(response.vendorOrderIds()).isNotEmpty();

        Long subOrderId = response.vendorOrderIds().get(0);

        // Assert Order Line Snapshots price
        var orderLines = orderLineRepository.findByVendorOrderId(subOrderId);
        assertThat(orderLines).hasSize(1);
        assertThat(orderLines.get(0).getUnitPriceMillimes()).isEqualTo(Money.ofMillimes(18500));
        assertThat(orderLines.get(0).getTotalMillimes()).isEqualTo(Money.ofMillimes(37000));

        // Assert Stock Committed (stock reduced by 2)
        Sku afterSku = skuRepository.findById(sku.getId()).orElseThrow();
        assertThat(afterSku.getStockOnHand()).isEqualTo(initialStock - 2);

        // Assert Invoice Generated with Gapless Numbering (Async via Spring Modulith listener)
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
            Optional<Invoice> invoice = invoicingService.findByVendorOrderId(subOrderId);
            assertThat(invoice).isPresent();
            assertThat(invoice.get().getInvoiceNumber()).contains("INV-V1-");
            assertThat(invoice.get().getFiscalStamp()).isEqualTo(Money.ofMillimes(1000));
        });

        // Assert Double-entry Ledger (Async via Spring Modulith listener)
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(() -> {
            var ledgerEntries = payoutLedgerService.getEntriesForReference("ORDER_PLACED", "ORDER-" + response.orderId());
            assertThat(ledgerEntries).hasSize(2); // 1 DEBIT (COD_RECEIVABLE_FROM_COURIER), 1 CREDIT (VENDOR_ESCROW)
        });

        // Assert Idempotency Key prevents duplicate orders
        CheckoutSagaService.CheckoutResponse retryResponse = checkoutSagaService.execute(cmd);
        assertThat(retryResponse.orderId()).isEqualTo(response.orderId());
        assertThat(retryResponse.paymentRef()).isEqualTo("IDEMPOTENT_RETRY");
    }
}
