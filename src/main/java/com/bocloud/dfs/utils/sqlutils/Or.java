package com.bocloud.dfs.utils.sqlutils;

public class Or extends CollectionOp {
    public Or(SqlCondition... conditions) {
        super("or", conditions);
    }
}
