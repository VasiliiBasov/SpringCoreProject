package com.vasilii.notificationhub;

import com.vasilii.notificationhub.api.ChannelType;
import com.vasilii.notificationhub.api.NotificationChannel;
import com.vasilii.notificationhub.channel.EmailConfig;
import com.vasilii.notificationhub.channel.EmailProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

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

            NotificationChannel sms = ctx.getBean("smsChannel", NotificationChannel.class);
            System.out.println("Бин: " + sms);

            Map<String, NotificationChannel> all = ctx.getBeansOfType(NotificationChannel.class);

            System.out.println("=== Все каналы через getBeansOfType: " + all.size() + " ===");
            all.forEach((name, ch) -> System.out.println(" " + name + " -> " + ch.getType()));

            // Спросим у BeanFactory, сколько вообще бинов зарегистрировано
            System.out.println("Всего бинов: " + ctx.getBeanDefinitionCount());

            EmailConfig cfg = ctx.getBean(EmailConfig.class);
            System.out.println("retryAttempts: " + cfg.getRetryAttempts());
            System.out.println("MillisecondsInMinute: " + cfg.getMillisecondsInMinute());
            System.out.println("isLocal: " + cfg.isLocal());

            EmailProperties props = ctx.getBean(EmailProperties.class);
            System.out.println("smtpHost (props): " + props.getSmtpHost());
            System.out.println("smtpPort (props): " + props.getSmtpPort());
            System.out.println("retryAttempts (props): " + props.getRetryAttempts());



            System.out.println("=== Контекст закрыт ===");
        }
    }
}
