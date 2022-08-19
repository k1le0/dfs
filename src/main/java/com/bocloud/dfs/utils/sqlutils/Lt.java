package com.bocloud.dfs.utils.sqlutils;

public class Lt extends BinaryOp {
    public Lt(String key, Object value) {
        super("<", key, value);
    }
}
