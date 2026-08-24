package com.vasilii.notificationhub;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "com.vasilii.notificationhub")
@PropertySource("classpath:application.properties")
public class AppConfig {
}
