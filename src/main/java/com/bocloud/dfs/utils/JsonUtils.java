package com.bocloud.dfs.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {
    private final static ObjectMapper om;

    static {
        om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule()
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer())
                        .addDeserializer(LocalDate.class, new LocalDateDeserializer()));
    }

    public static String toJson(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return om.writeValueAsString(o);
        } catch (Exception e) {
            log.error("tojson failed,class={}", o.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String s, Class<T> clazz) {
        try {
            return om.readValue(s, clazz);
        } catch (IOException e) {
            log.error("fromjson failed,str={},class={}", s, clazz.getSimpleName());
            throw new RuntimeException(e);
        }
    }

//    public static <T> T fromJson(String s, TypeReference<?> toValueTypeRef) {
//        try {
//            return om.readValue(s, toValueTypeRef);
//        } catch (IOException e) {
//            log.error("fromjson failed,str={},class={}", s, toValueTypeRef);
//            throw new RuntimeException(e);
//        }



    public static Map<String, Object> toMap(String s) {
        try {
            return om.readValue(s, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            log.error("toMap failed,str={}", s);
            throw new RuntimeException(e);
        }
    }


    public static Map<String, Object> toMap(Object s) {
        try {
            return om.convertValue(s, new TypeReference<Map<String, Object>>() {
            });
        } catch (IllegalArgumentException e) {
            log.error("toMap failed,str={}", s);
            throw new RuntimeException(e);
        }
    }

    public static List<String> toList(Object s) {
        try {
            return om.convertValue(s, new TypeReference<List<String>>() {
            });
        } catch (IllegalArgumentException e) {
            log.error("toMap failed,str={}", s);
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertValue(Object s, Class<T> clazz) {
        try {
            return om.convertValue(s, clazz);
        } catch (IllegalArgumentException e) {
            log.error("toMap failed,str={}", s);
            throw new RuntimeException(e);
        }
    }
}
