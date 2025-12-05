package vn.hbtplus.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;

@Component
public class I18n {
    private static ResourceBundleMessageSource messageSource;

    @Autowired
    I18n(ResourceBundleMessageSource messageSource) {
        I18n.messageSource = messageSource;
    }

    public static String getMessage(String msgCode) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.getLanguage() == null || locale.getLanguage().isEmpty()) {
            locale = new Locale("vi");
        }
        try {
            return messageSource.getMessage(msgCode, null, locale);
        } catch (MissingResourceException e) {
            return msgCode;
        }
    }

    public static String getMessage(String msgCode, Object... arg) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.getLanguage() == null || locale.getLanguage().isEmpty()) {
            locale = new Locale("vi");
        }
        String messageKey;
        try {
            messageKey = messageSource.getMessage(msgCode, null, locale);
        } catch (MissingResourceException e) {
            messageKey = msgCode;
        }
        if (messageKey.contains("{0}")) {
            return MessageFormat.format(messageKey, arg);
        } else {
            return String.format(messageKey, arg);
        }
    }
}
