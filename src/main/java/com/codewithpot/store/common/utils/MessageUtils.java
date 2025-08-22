package com.codewithpot.store.common.utils;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class MessageUtils {
    private final static Locale localEn = Locale.forLanguageTag("en");

    public static String getMessage(MessageSource messageSource, String messageCode) {

        String message = "";
        message = messageSource.getMessage(messageCode, null, localEn);

        return message;
    }
}
