package com.bocloud.dfs.utils.sqlutils;

public class Between implements SqlCondition {
    private String key;
    private Object v1;
    private Object v2;

    public Between(String key, Object v1, Object v2) {
        this.key = key;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toSql(SqlContext context) {
        return key + " between {" + context.bind(v1) + "} and {" + context.bind(v2) + "}";
    }

    @Override
    public SqlCondition clone() {
        return new Between(key, v1, v2);
    }
}
