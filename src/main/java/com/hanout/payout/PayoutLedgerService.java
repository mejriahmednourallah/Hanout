package com.hanout.payout;

import com.hanout.common.Money;
import com.hanout.ordering.OrderPlacedEvent;
import com.hanout.ordering.VendorOrderDeliveredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PayoutLedgerService {

    private static final Logger log = LoggerFactory.getLogger(PayoutLedgerService.class);

    private final LedgerEntryRepository ledgerRepository;

    public PayoutLedgerService(LedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @ApplicationModuleListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        String refId = "ORDER-" + event.orderId();
        boolean isCod = "COD".equalsIgnoreCase(event.paymentGateway());

        LedgerAccount debitAccount = isCod ? LedgerAccount.COD_RECEIVABLE_FROM_COURIER : LedgerAccount.PAYMENT_CLEARING;

        // Double entry: DEBIT receivable/clearing, CREDIT vendor escrow
        ledgerRepository.save(new LedgerEntry(debitAccount, event.totalAmount(), LedgerDirection.DEBIT, "ORDER_PLACED", refId));
        ledgerRepository.save(new LedgerEntry(LedgerAccount.VENDOR_ESCROW, event.totalAmount(), LedgerDirection.CREDIT, "ORDER_PLACED", refId));

        log.info("Recorded initial ledger entries for Order #{}", event.orderId());
    }

    @ApplicationModuleListener
    public void onVendorOrderDelivered(VendorOrderDeliveredEvent event) {
        String refId = "VENDOR_ORDER-" + event.vendorOrderId();
        Money netVendorPayable = event.subTotal().minus(event.commissionAmount());

        // Escrow released: DEBIT VENDOR_ESCROW
        ledgerRepository.save(new LedgerEntry(LedgerAccount.VENDOR_ESCROW, event.subTotal(), LedgerDirection.DEBIT, "SUBORDER_DELIVERED", refId));

        // Credit Vendor Payable
        ledgerRepository.save(new LedgerEntry(LedgerAccount.VENDOR_PAYABLE, netVendorPayable, LedgerDirection.CREDIT, "SUBORDER_DELIVERED", refId));

        // Credit Platform Commission
        ledgerRepository.save(new LedgerEntry(LedgerAccount.PLATFORM_COMMISSION, event.commissionAmount(), LedgerDirection.CREDIT, "SUBORDER_DELIVERED", refId));

        log.info("Delivered sub-order #{} settlement entries recorded: Vendor Payable = {}, Commission = {}",
                event.vendorOrderId(), netVendorPayable, event.commissionAmount());
    }

    @Transactional
    public void settleCourierRemittance(String trackingNo, Long vendorOrderId, Money amountCollected) {
        String refId = "REMITTANCE-" + trackingNo;
        // Courier remittance received: Credit COD receivable, Debit Payment Clearing
        ledgerRepository.save(new LedgerEntry(LedgerAccount.COD_RECEIVABLE_FROM_COURIER, amountCollected, LedgerDirection.CREDIT, "COURIER_REMITTANCE", refId));
        ledgerRepository.save(new LedgerEntry(LedgerAccount.PAYMENT_CLEARING, amountCollected, LedgerDirection.DEBIT, "COURIER_REMITTANCE", refId));
    }

    public List<LedgerEntry> getEntriesForReference(String refType, String refId) {
        return ledgerRepository.findByRefTypeAndRefId(refType, refId);
    }

    public List<LedgerEntry> getAccountEntries(LedgerAccount account) {
        return ledgerRepository.findByAccount(account);
    }
}
