package com.vasilii.notificationhub.api;

public interface NotificationChannel {
    ChannelType getType();
    void send(String recipient, String message);
}
