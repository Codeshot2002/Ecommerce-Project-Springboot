package com.ecommerce.project.service;

import org.springframework.stereotype.Service;

@Service("email")
public class EmailNotification implements NotificationService {
    @Override
    public void send() {
        System.out.println("Sending Email Notification");
    }
}
