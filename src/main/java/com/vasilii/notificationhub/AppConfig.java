package com.vasilii.notificationhub;

import com.vasilii.notificationhub.channel.EmailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.vasilii.notificationhub")
@PropertySource("classpath:override.properties")
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties(EmailProperties.class)
public class AppConfig {

}
