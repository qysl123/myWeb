package com.zk.common;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.zk.enums.BaseEnum;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 *
 * Created by Ken on 2016/7/21.
 */
public class BaseEnumSerializer {}
//        implements ObjectSerializer, ObjectDeserializer{
//    @Override
//    public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
//        SerializeWriter out = jsonSerializer.out;
//        BaseEnum baseEnum = (BaseEnum)o;
//        out.write("{\"id\":\""+baseEnum.getId()+"\",\"text\":\""+baseEnum.getText()+"\"}");
//    }
//
//
//    @Override
//    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
//        Class<T> s = (Class)type;
//        for (T enums : s.getEnumConstants()){
//            BaseEnum baseEnum = (BaseEnum)enums;
//            if(baseEnum.getId() == Integer.valueOf(defaultJSONParser.getLexer().stringVal())){
//                return (T)baseEnum;
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public int getFastMatchToken() {
//        return 0;
//    }
//}
