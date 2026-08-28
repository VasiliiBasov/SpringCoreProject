package com.vasilii.notificationhub.channel;

import com.vasilii.notificationhub.api.ChannelType;
import com.vasilii.notificationhub.api.NotificationChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsChannel implements NotificationChannel {
    private final String gatewayUrl;

    public SmsChannel(@Value("${app.channels.sms.gatewayUrl:http://localhost:8080/sms}")String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
        System.out.println("[ctor] SmsChannel создан с gatewayUrl=" + gatewayUrl);
    }
    @Override
    public void send(String recipient, String message) {
        System.out.println("[SMS] -> " + recipient + " : " + message
                + "  (via " + gatewayUrl + ")");
    }
    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }

    @PostConstruct
    public void postConstructMethod() {
        System.out.println("[PostConstruct] SmsChannel инициализирован со gatewayUrl =  " + gatewayUrl);
    }

    @PreDestroy
    public void preDestroyMethod() {
        System.out.println("[PreDestroy] SmsChannel уничтожается...");
    }
}
