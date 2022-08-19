package com.bocloud.dfs.utils.sqlutils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Field {
    String value();
}
