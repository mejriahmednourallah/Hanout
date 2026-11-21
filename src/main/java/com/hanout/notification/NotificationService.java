package com.hanout.notification;

import com.hanout.ordering.OrderPlacedEvent;
import com.hanout.ordering.VendorOrderShippedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public NotificationService(SmsSender smsSender, EmailSender emailSender) {
        this.smsSender = smsSender;
        this.emailSender = emailSender;
    }

    @ApplicationModuleListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Sending notifications for Order #{}", event.orderId());
        String sms = NotificationTemplate.getOrderPlacedMessage("ar", event.orderId(), event.totalAmount().toDinarString());
        smsSender.sendSms(event.phone(), sms);
    }

    @ApplicationModuleListener
    public void onSubOrderShipped(VendorOrderShippedEvent event) {
        log.info("Sending shipping notification for SubOrder #{}", event.vendorOrderId());
        String sms = NotificationTemplate.getOrderShippedMessage("ar", event.vendorOrderId(), event.trackingNumber());
        smsSender.sendSms("+21620111222", sms);
    }
}
