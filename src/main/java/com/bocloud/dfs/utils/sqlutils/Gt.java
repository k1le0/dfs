package com.bocloud.dfs.utils.sqlutils;

public class Gt extends BinaryOp {
    public Gt(String key, Object value) {
        super(">", key, value);
    }
}
