package com.bocloud.dfs.utils.sqlutils;

public interface SqlCondition {
    String toSql(SqlContext context);
    SqlCondition clone();
}
