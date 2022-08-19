package com.bocloud.dfs.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer2 extends JsonSerializer<BigDecimal> {
    @Override
    @SuppressWarnings("all")
    public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String str = value.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        jgen.writeString(trim(str));
    }

    @SuppressWarnings("all")
    public static String trim(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        if (str.contains(".")) {
            for (int i = 0; i < str.length(); ++i) {
                char ch = str.charAt(str.length() - i - 1);
                if (ch == '0') {
                    continue;
                } else if (ch == '.') {
                    str = str.substring(0, str.length() - i - 1);
                    break;
                } else {
                    str = str.substring(0, str.length() - i);
                    break;
                }
            }
        }
        return str;
    }
}
