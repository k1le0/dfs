package com.bocloud.dfs.utils.sqlutils;

public class Ge extends BinaryOp {
    public Ge(String key, Object value) {
        super(">=", key, value);
    }
}
