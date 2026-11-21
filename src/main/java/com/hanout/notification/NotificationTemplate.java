package com.hanout.notification;

public class NotificationTemplate {

    public static String getOrderPlacedMessage(String locale, Long orderId, String amount) {
        if ("fr".equalsIgnoreCase(locale)) {
            return String.format("Votre commande #%d d'un montant de %s TND a été enregistrée avec succès.", orderId, amount);
        } else if ("en".equalsIgnoreCase(locale)) {
            return String.format("Your order #%d for %s TND has been placed successfully.", orderId, amount);
        } else {
            // Arabic with RTL mark
            return String.format("\u200Fتم تسجيل طلبك رقم #%d بنجاح بمبلغ %s دينار تونسي. شكرا لتسوقك في حانوت.", orderId, amount);
        }
    }

    public static String getOrderShippedMessage(String locale, Long subOrderId, String trackingNo) {
        if ("fr".equalsIgnoreCase(locale)) {
            return String.format("Votre colis pour la sous-commande #%d est expédié. N° de suivi: %s", subOrderId, trackingNo);
        } else if ("en".equalsIgnoreCase(locale)) {
            return String.format("Your package for sub-order #%d has been shipped. Tracking: %s", subOrderId, trackingNo);
        } else {
            // Arabic with RTL mark
            return String.format("\u200Fتم شحن طردك للطلب الفرعي رقم #%d. رقم التتبع: %s", subOrderId, trackingNo);
        }
    }
}
