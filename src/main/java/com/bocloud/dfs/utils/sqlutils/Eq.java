package com.bocloud.dfs.utils.sqlutils;

public class Eq extends BinaryOp {
    public Eq(String key, Object value) {
        super("=", key, value);
    }

    @Override
    public String toSql(SqlContext context) {
        if (value == null || value instanceof DbNull) {
            return key + " is null";
        } else {
            return key + op + "{" + context.bind(value) + "}";
        }
    }
}
