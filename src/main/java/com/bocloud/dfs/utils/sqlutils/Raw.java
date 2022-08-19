package com.bocloud.dfs.utils.sqlutils;

public class Raw implements SqlCondition {
    private String sql;
    private Object[] values;

    public Raw(String sql, Object... values) {
        this.sql = sql;
        this.values = values;
    }

    @Override
    public String toSql(SqlContext context) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (int i = 0; i < sql.length(); ++i) {
            char ch = sql.charAt(i);
            if (ch == '?') {
                sb.append("{").append(context.bind(values[pos++])).append("}");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    public SqlCondition clone() {
        return new Raw(sql, values);
    }
}
