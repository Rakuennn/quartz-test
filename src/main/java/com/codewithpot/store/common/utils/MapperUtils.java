package com.codewithpot.store.common.utils;

import org.springframework.beans.BeanUtils;

public class MapperUtils {
    public static <T> T projectTo(Object source, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
