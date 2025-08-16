package com.mxh.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    @Autowired
    private MessageSource messageSource;

    /**
     * 根据当前上下文（默认使用请求的Locale）获取消息
     * @param code 消息键
     * @param args 消息参数
     * @return 解析后的消息
     */
    public String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * 强制指定Locale获取消息
     * @param code 消息键
     * @param args 消息参数
     * @param locale 语言区域
     * @return 解析后的消息
     */
    public String getMessageWithLocale(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }
}

