package com.bocloud.dfs.utils.sqlutils;

public class BinaryOp implements SqlCondition {
    protected String op;
    protected String key;
    protected Object value;

    public BinaryOp(String op, String key, Object value) {
        this.op = op;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toSql(SqlContext context) {
        return key + op + "{" + context.bind(value) + "}";
    }

    @Override
    public SqlCondition clone() {
        return new BinaryOp(op, key, value);
    }
}
