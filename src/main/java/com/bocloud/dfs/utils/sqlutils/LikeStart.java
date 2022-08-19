package com.bocloud.dfs.utils.sqlutils;

public class LikeStart extends BinaryOp {
    public LikeStart(String key, Object value) {
        super(" like ", key, value + "%");
    }
}
