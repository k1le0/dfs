package com.bocloud.dfs.utils.sqlutils;

import java.sql.SQLException;
import java.util.Map;

public interface RowFunc {
    void onRow(Map<String, Object> objectMap, DataRow row) throws SQLException;
}
