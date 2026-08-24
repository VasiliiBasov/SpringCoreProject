package com.vasilii.notificationhub;

import com.vasilii.notificationhub.api.NotificationChannel;
import com.vasilii.notificationhub.channel.EmailChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.vasilii.notificationhub")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${app.channels.email.smtp:smtp.local}")
    private String emailSmtp;

    @Bean
    public NotificationChannel emailChannel() {
        return new EmailChannel(emailSmtp);
    }
}
