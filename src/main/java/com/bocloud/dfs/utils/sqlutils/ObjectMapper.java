package com.bocloud.dfs.utils.sqlutils;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanMap;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.*;

@Slf4j
public class ObjectMapper {
    private Map<String, ColumnInfo> columns;
    static class ColumnInfo {
        String tableAlias;
        String tableField;
        String objectField;
    }

    /**
     * 将行集映射为对象
     *
     * @param objectMap 对象表Map<别名，对象>,别名为*，表示不采用别名模式
     * @param rs        结果集
     * @throws SQLException
     */
    public void mapper(Map<String, Object> objectMap, ResultSet rs) throws SQLException {
        if (columns == null) {
            ResultSetMetaData metaData = rs.getMetaData();
            columns = new LinkedHashMap<>(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                ColumnInfo columnInfo = new ColumnInfo();
                String label = metaData.getColumnLabel(i);
                String[] items= label.split("@");
                if (items.length == 1) {
                    columnInfo.tableAlias = "*";
                    columnInfo.tableField = items[0];
                } else {
                    columnInfo.tableAlias = items[0];
                    columnInfo.tableField = items[1];
                }
                columns.put(metaData.getColumnLabel(i), columnInfo);
            }
        }

        List<String> ignoreColumns = null;

        for (Map.Entry<String, ColumnInfo> column : columns.entrySet()) {
            ColumnInfo columnInfo = column.getValue();
            Object o = objectMap.get(columnInfo.tableAlias);
            if (o == null) {
                // 行集字段没在objectMap中找到
                if (ignoreColumns == null) {
                    ignoreColumns = new ArrayList<>();
                }
                ignoreColumns.add(column.getKey());
                // 忽略该字段
                continue;
            }
            BeanMap.Generator generator = new BeanMap.Generator();
            generator.setBean(o);
            generator.setRequire(BeanMap.REQUIRE_SETTER);
            BeanMap bm = generator.create();
            if (columnInfo.objectField == null) {
                // 映射对象的哪个字段还不清楚
                String fieldName = columnInfo.tableField;
                if (!bm.containsKey(fieldName)) {
                    fieldName = Utils.toPropertyName(columnInfo.tableField);
                }
                if (!bm.containsKey(fieldName)) {
                    // 对象字段不可写
                    if (ignoreColumns == null) {
                        ignoreColumns = new ArrayList<>();
                    }
                    ignoreColumns.add(column.getKey());
                    // not found in bean
                    continue;
                } else {
                    columnInfo.objectField = fieldName;
                }
            }
            Class<?> parameterType = bm.getPropertyType(columnInfo.objectField);
            Object value;
            if (parameterType == int.class) {
                value = rs.getInt(column.getKey());
            } else if (parameterType == short.class) {
                value = rs.getShort(column.getKey());
            } else if (parameterType == long.class) {
                value = rs.getLong(column.getKey());
            } else if (parameterType == float.class) {
                value = rs.getFloat(column.getKey());
            } else if (parameterType == double.class) {
                value = rs.getDouble(column.getKey());
            } else if (parameterType == Short.class) {
                value = rs.getShort(column.getKey());
                if (rs.wasNull()) {
                    value = null;
                }
            } else if (parameterType == Integer.class) {
                value = rs.getInt(column.getKey());
                if (rs.wasNull()) {
                    value = null;
                }
            } else if (parameterType == Long.class) {
                value = rs.getLong(column.getKey());
                if (rs.wasNull()) {
                    value = null;
                }
            } else if (parameterType == Float.class) {
                value = rs.getFloat(column.getKey());
                if (rs.wasNull()) {
                    value = null;
                }
            } else if (parameterType == Double.class) {
                value = rs.getDouble(column.getKey());
                if (rs.wasNull()) {
                    value = null;
                }
            } else if (parameterType == LocalDate.class) {
                java.sql.Date date = rs.getDate(column.getKey());
                if (date != null) {
                    value = date.toLocalDate();
                } else {
                    value = null;
                }
            } else if (parameterType == LocalDateTime.class) {
                Timestamp value1 = rs.getTimestamp(column.getKey());
                if (value1 == null) {
                    value = null;
                } else {
                    value = value1.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            } else if (parameterType == LocalTime.class) {
                Time time = rs.getTime(column.getKey());
                value = time.toLocalTime();
            } else if (parameterType == Date.class) {
                Timestamp value1 = rs.getTimestamp(column.getKey());
                if (value1 != null) {
                    value = new Date(value1.getTime());
                } else {
                    value = null;
                }
            } else if (parameterType == String.class) {
                value = rs.getString(column.getKey());
            } else if (parameterType == BigDecimal.class) {
                value = rs.getBigDecimal(column.getKey());
            } else {
                if (parameterType != List.class && parameterType != Map.class) {
                    value = rs.getObject(column.getKey());
                    log.warn("skip unknown mapper,field={},value={},type={}", columnInfo.objectField, value, parameterType.getName());
                }
                if (ignoreColumns == null) {
                    ignoreColumns = new ArrayList<>();
                }
                ignoreColumns.add(column.getKey());
                value = null;
            }
            bm.put(columnInfo.objectField, value);
        }

        if (ignoreColumns != null) {
            for (String ignoreColumn : ignoreColumns) {
                columns.remove(ignoreColumn);
            }
            ignoreColumns.clear();
        }
    }
}
