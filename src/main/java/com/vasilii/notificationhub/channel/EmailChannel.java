package com.vasilii.notificationhub.channel;

import com.vasilii.notificationhub.api.ChannelType;
import com.vasilii.notificationhub.api.NotificationChannel;

public class EmailChannel implements NotificationChannel {
    private final String smtpHost;

    public EmailChannel(String smtpHost) {
        this.smtpHost = smtpHost;
        System.out.println("[ctor] EmailChannel создан с smtpHost=" + smtpHost);
    }
    @Override
    public void send(String recipient, String message) {
        System.out.println("[EMAIL] -> " + recipient + " : " + message
                + "  (via " + smtpHost + ")");
    }
    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }
}
