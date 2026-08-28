package com.ecommerce.project.service;

import org.springframework.stereotype.Service;

@Service("sms")
public class SmsNotification implements NotificationService {
    @Override
    public void send() {
        System.out.println("Sending SMS Notification");
    }
}
