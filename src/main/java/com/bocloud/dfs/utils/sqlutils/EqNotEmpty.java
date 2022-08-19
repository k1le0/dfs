package com.bocloud.dfs.utils.sqlutils;

public class EqNotEmpty implements SqlCondition {
    private String key;
    private Object value;

    public EqNotEmpty(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toSql(SqlContext context) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            if (((String)value).isEmpty()) {
                return null;
            }
        }
        return key + "={" + context.bind(value) + "}";
    }

    @Override
    public SqlCondition clone() {
        return new EqNotEmpty(key, value);
    }
}
