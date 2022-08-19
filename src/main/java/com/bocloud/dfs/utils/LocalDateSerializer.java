package com.bocloud.dfs.utils;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer extends JsonSerializer<LocalDate> {
    private final static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.ofHours(8));

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String str = value.format(format);
        gen.writeString(str);
    }
}
