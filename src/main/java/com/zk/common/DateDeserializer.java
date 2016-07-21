package com.zk.common;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Ken on 2016/7/20.
 */
@Component("dateDeserializer")
public class DateDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        System.out.println("json date Deserializer");
        String ss = defaultJSONParser.getLexer().stringVal();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return (T)sdf.parse(ss);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
