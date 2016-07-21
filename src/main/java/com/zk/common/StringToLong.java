package com.zk.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;


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

    public static void main(String[] args){
        System.out.println(URLDecoder.decode("http://hy6.nbark.com:7602/sms.aspx?userid=363&account=fzqiankunhchy6&password=goodcar2015bruce&sendTime=&extno=&action=send&mobile=15528005592&content=%E5%B0%8A%E6%95%AC%E7%9A%84%E6%9B%B9%E6%93%8D%E6%82%A8%E5%A5%BD%EF%BC%8C%E4%B9%BE%E5%9D%A4%E5%A5%BD%E8%BD%A6%E8%AF%B7%E6%82%A8%E5%AF%B9%E5%8C%97%E4%BA%AC%E5%A5%94%E9%A9%B0%202015%E6%AC%BE%20%E5%A5%94%E9%A9%B0C%E7%BA%A7%20%E6%94%B9%E6%AC%BE%20C%20180%20L%E3%80%90VIN:LBV59300088887123%E3%80%91%E8%BF%9B%E8%A1%8C%E8%BD%A6%E9%99%A9%E6%8A%A5%E4%BB%B7,%E6%8A%A5%E4%BB%B7%E5%9C%B0%E5%9D%80:http://192.168.10.78:91/carInsurance/quoteDetail?id%3D27%20%E3%80%90%E4%B9%BE%E5%9D%A4%E5%A5%BD%E8%BD%A6%E3%80%91"));
    }
}
