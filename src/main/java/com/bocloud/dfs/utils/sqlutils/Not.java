package com.bocloud.dfs.utils.sqlutils;

public class Not implements SqlCondition {
    private SqlCondition condition;

    public Not(SqlCondition condition) {
        this.condition = condition;
    }

    @Override
    public String toSql(SqlContext context) {
        return "not(" + condition.toSql(context) + ")";
    }

    @Override
    public SqlCondition clone() {
        return new Not(condition);
    }
}
