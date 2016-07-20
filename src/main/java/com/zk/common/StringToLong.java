package com.zk.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;


public class StringToLong implements Converter<String,Long> {
    @Override
    public Long convert(String s) {
        System.out.println("Long Converter");
        if (StringUtils.isEmpty(s)) {
            return 0L;
        } else {
            return Long.parseLong(s);
        }
    }
}
