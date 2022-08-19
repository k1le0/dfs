package com.bocloud.dfs.utils.sqlutils;

public class And extends CollectionOp {
    public And(SqlCondition... conditions) {
        super("and", conditions);
    }
}
