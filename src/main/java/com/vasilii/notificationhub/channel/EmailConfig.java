package com.vasilii.notificationhub.channel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailConfig {
    //Value сколько миллисекунд в минуте
    @Value("#{1000*60}")
    private long millisecondsInMinute;

    //дефолт 3, если свойства нет
    @Value("${email.retry-attempts:3}")
    private int retryAttempts;

    //SpEL тернарный оператор
    @Value("${email.smtp-host}")
    private String smtpHost;

    public boolean isLocal() {
        return "smtp.local".equals(smtpHost);
    }

    public long getMillisecondsInMinute() {
        return millisecondsInMinute;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

}
