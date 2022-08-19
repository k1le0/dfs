package com.bocloud.dfs.utils.sqlutils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataRow {
    private ResultSet rst;

    DataRow(ResultSet rst) {
        this.rst = rst;
    }

    public String getString(String name) throws SQLException {
        return rst.getString(name);
    }

    public String getString(String alias, String name) throws SQLException {
        return rst.getString(alias + "@" + name);
    }

    public Integer getInt(String name) throws SQLException {
        int i = rst.getInt(name);
        if (rst.wasNull()) {
            return null;
        }
        return i;
    }

    public Integer getInt(String alias, String name) throws SQLException {
        int i = rst.getInt(alias + "@" + name);
        if (rst.wasNull()) {
            return null;
        }
        return i;
    }

    public Long getLong(String name) throws SQLException {
        long i = rst.getLong(name);
        if (rst.wasNull()) {
            return null;
        }
        return i;
    }

    public Long getLong(String alias, String name) throws SQLException {
        long i = rst.getLong(alias + "@" + name);
        if (rst.wasNull()) {
            return null;
        }
        return i;
    }

    public Date getDate(String name) throws SQLException {
        return rst.getDate(name);
    }

    public Date getDate(String alias, String name) throws SQLException {
        return rst.getDate(alias + "@" + name);
    }
}
