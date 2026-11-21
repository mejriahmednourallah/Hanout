package com.hanout.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockNotificationSenders implements SmsSender, EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationSenders.class);

    @Override
    public void sendSms(String toPhone, String text) {
        log.info("[SMS -> {}]: {}", toPhone, text);
    }

    @Override
    public void sendEmail(String toEmail, String subject, String bodyHtml) {
        log.info("[EMAIL -> {}] Subject: {} | Body: {}", toEmail, subject, bodyHtml);
    }
}
