package com.vasilii.notificationhub;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            System.out.println("=== Notification Hub стартовал ===");
            System.out.println("Имя приложения: " + ctx.getApplicationName());
            System.out.println("Дата старта: " + ctx.getStartupDate());
            System.out.println("=== Контекст закрыт ===");
        }
    }
}
