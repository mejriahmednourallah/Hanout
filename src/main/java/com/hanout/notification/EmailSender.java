package com.hanout.notification;

public interface EmailSender {
    void sendEmail(String toEmail, String subject, String bodyHtml);
}
