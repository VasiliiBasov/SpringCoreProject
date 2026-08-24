package com.vasilii.notificationhub;

import com.vasilii.notificationhub.api.ChannelType;
import com.vasilii.notificationhub.api.NotificationChannel;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            System.out.println("=== Notification Hub стартовал ===");

            // Получаем бин по id (имя @Bean-метода = emailChannel)
            NotificationChannel email = ctx.getBean("emailChannel", NotificationChannel.class);
            System.out.println("Бин: " + email);
            System.out.println("Тип канала: " + email.getType());

            // Проверяем, что EmailChannel в контексте только один (singleton)
            NotificationChannel email2 = ctx.getBean("emailChannel", NotificationChannel.class);
            System.out.println("Тот же самый? " + (email == email2));

            // Отправляем "уведомление"
            email.send("alice@example.com", "Добро пожаловать в Notification Hub!");

            // Считаем все бины типа NotificationChannel
            String[] channelNames = ctx.getBeanNamesForType(NotificationChannel.class);
            System.out.println("Каналов в контексте: " + channelNames.length);
            for (String name : channelNames) {
                System.out.println("  - " + name);
            }

            // Спросим у BeanFactory, сколько вообще бинов зарегистрировано
            System.out.println("Всего бинов: " + ctx.getBeanDefinitionCount());

            System.out.println("=== Контекст закрыт ===");
        }
    }
}
