package com.hanout.notification;

public interface SmsSender {
    void sendSms(String toPhone, String text);
}
