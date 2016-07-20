package com.zk.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.zk.enums.BaseEnum;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Created by Ken on 2016/7/18.
 */
@Component("customObjectMapper")
public class CustomObjectMapper extends ObjectMapper {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public CustomObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());
        module.addSerializer(BaseEnum.class, new ItemSerializer());
        module.addDeserializer(Date.class, new DateDeserializer());
        this.registerModule(module);
    }

    class DateDeserializer extends JsonDeserializer<Date>{

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            try {
                String s = jsonParser.getText();
                return sdf.parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class DateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeString(sdf.format(value));
        }
    }
    class ItemSerializer extends JsonSerializer<BaseEnum> {

        @Override
        public void serialize(BaseEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            gen.writeObjectField("id", value.getId());
            gen.writeObjectField("text", value.getText());
            gen.writeEndObject();
        }
    }
}