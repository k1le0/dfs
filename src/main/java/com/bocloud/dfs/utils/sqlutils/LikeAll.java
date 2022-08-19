package com.bocloud.dfs.utils.sqlutils;

public class LikeAll extends BinaryOp {
    public LikeAll(String key, Object value) {
        super(" like ", key, "%" + value + "%");
    }
}
