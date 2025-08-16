package com.mxh.bank.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class I18nConfig {

    /**
     * 配置 MessageSource，加载国际化资源文件
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // 指定资源文件路径（classpath:messages 对应 messages.properties 系列文件）
        messageSource.setBasename("classpath:i18n/message");
        // 资源文件编码（解决中文乱码）
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * 配置 LocaleResolver，确定当前语言环境
     * 这里使用 AcceptHeaderLocaleResolver：从请求头 "Accept-Language" 获取语言
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        return resolver;
    }

    /**
     * 让 Validator（参数校验）支持国际化消息
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
