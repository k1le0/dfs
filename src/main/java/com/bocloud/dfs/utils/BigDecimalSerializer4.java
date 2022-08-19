package com.bocloud.dfs.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer4 extends JsonSerializer<BigDecimal> {
    @Override
    @SuppressWarnings("all")
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String str = value.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        jgen.writeString(BigDecimalSerializer2.trim(str));
    }
}
