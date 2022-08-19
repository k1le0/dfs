package com.bocloud.dfs.utils.sqlutils;

import java.util.Collection;

public class In implements SqlCondition {
    private String key;
    private Collection value;

    public In(String key, Collection value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toSql(SqlContext context) {
        if (value == null || value.isEmpty()) {
            return " false ";
        }
        return key + " in ({" + context.bind(value) + "})";
    }

    @Override
    public SqlCondition clone() {
        return new In(key, value);
    }
}
