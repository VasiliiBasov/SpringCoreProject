package com.vasilii.notificationhub.processor;

import com.vasilii.notificationhub.api.NotificationChannel;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TimingBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof NotificationChannel) {
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice(new TimingMethodInterceptor());
            Object proxy = proxyFactory.getProxy();
            System.out.println("[Timing] Прокси для " + beanName + ": " + proxy.getClass().getName());
            return proxy;

        }
        return bean;
    }

    private static class TimingMethodInterceptor implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            long start = System.nanoTime();
            Object result = invocation.proceed();
            long duration = (System.nanoTime() - start) / 1000;
            System.out.println("[Timing] " + invocation.getMethod().getName() + "() занял " + duration + " мкс");
            return result;
        }
    }
}
