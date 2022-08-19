package com.bocloud.dfs.utils.sqlutils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SqlContext {
    private int i;
    private Map<String, Object> values;

    public SqlContext() {
        i = 0;
        values = new HashMap<>();
    }

    public SqlContext(SqlContext context) {
        i = context.i;
        values = context.values;
    }

    public String bind(Object value) {
        i++;
        String name = "__sqlbuilder__" + i;
        values.put(name, value);
        return name;
    }

    public Object get(String name) {
        return values.get(name);
    }

    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(values);
    }
}
