package com.vasilii.notificationhub.channel;

import com.vasilii.notificationhub.api.ChannelType;
import com.vasilii.notificationhub.api.NotificationChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class EmailChannel implements NotificationChannel {
    private final String smtpHost;

    private long millisecondsInHour = 60;

    public EmailChannel(@Value("${app.channels.email.smtp:smtp.local}")String smtpHost) {
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

    @PostConstruct
    public void postConstructMethod() {
        System.out.println("[PostConstruct] EmailChannel инициализирован со smtpHost =  " + smtpHost);
    }

    @PreDestroy
    public void preDestroyMethod() {
        System.out.println("[PreDestroy] EmailChannel уничтожается...");
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public long getMillisecondsInHour() {
        return millisecondsInHour;
    }
}
