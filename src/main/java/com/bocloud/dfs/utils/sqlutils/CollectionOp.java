package com.bocloud.dfs.utils.sqlutils;

import java.util.ArrayList;
import java.util.Arrays;

public class CollectionOp implements SqlCondition {
    private String op;
    private ArrayList<SqlCondition> values;

    public CollectionOp(String op, SqlCondition... conditions) {
        this.op = op;
        values = new ArrayList<>(Arrays.asList(conditions));
    }

    public void add(SqlCondition... condition) {
        this.values.addAll(Arrays.asList(condition));
    }

    public SqlCondition get(int i) {
        return values.get(i);
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toSql(SqlContext context) {
        StringBuilder sb = new StringBuilder();
        for (SqlCondition c : values) {
            if (c == null) {
                continue;
            }
            String s = c.toSql(context);
            if (s != null && !s.isEmpty()) {
                if (sb.length() != 0) {
                    sb.append(" ").append(op).append(" ");
                }
                if (c instanceof CollectionOp) {
                    if ((this instanceof And) && (c instanceof Or)) {
                        sb.append("(");
                        sb.append(s);
                        sb.append(")");
                    } else {
                        sb.append(s);
                    }
                } else {
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public SqlCondition clone() {
        CollectionOp result = new CollectionOp(op);
        result.values = new ArrayList<>(values);
        return result;
    }
}
