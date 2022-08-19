package com.bocloud.dfs.utils;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private final static DateTimeFormatter format = DateTimeFormatter.ofPattern("[yyyy-MM-dd[ HH:mm[:ss][.SSS]]][HH:mm[:ss][.SSS]]");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (StringUtils.isEmpty(p.getText())) {
            return null;
        }
        return LocalDateTime.parse(p.getText(), format);
    }
}
