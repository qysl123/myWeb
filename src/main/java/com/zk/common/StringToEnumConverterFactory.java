package com.zk.common;

import com.zk.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;


public class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {
    StringToEnumConverterFactory() {
    }

    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverterFactory.StringToEnum(targetType);
    }

    private class StringToEnum<T extends Enum> implements Converter<String, T> {
        private final Class<T> enumType;

        public StringToEnum(Class<T> var1) {
            this.enumType = var1;
        }

        public T convert(String source) {
            System.out.println("enum Converter");
            T[] enums = enumType.getEnumConstants();
            for (T baseEnum : enums) {
                if(baseEnum.ordinal() == Integer.valueOf(source)){
                    return baseEnum;
                }
            }
            return enums[2];
        }
    }
}
