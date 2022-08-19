package com.bocloud.dfs.utils.sqlutils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库表源数据信息
 * @author xubin
 */
@Slf4j
public class TableMetadata {
    private static Map<String, Map<String, Integer>> tableFieldsMapper = new ConcurrentHashMap<>();

    /**
     * 获取表的select语句
     *
     * @param table        表名
     * @param alias        别名，可以为空
     * @param jdbcTemplate
     * @return 字段名列表
     */
    public static List<String> getSelect(String table, String alias, JdbcTemplate jdbcTemplate) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> fields = getFields(table, jdbcTemplate);
        for (String key : fields.keySet()) {
            if (alias == null) {
                result.add("`"+key+"`");
            } else {
                result.add(alias + "." +"`"+key+"`" + " `" + alias + "@" + key + "`");
            }
        }
        return result;
    }


    /**
     * 返回数据库表的源数据信息 Map<字段名,类型>
     *
     * @param table        表名
     * @param jdbcTemplate
     * @return
     */
    public static Map<String, Integer> getFields(String table, JdbcTemplate jdbcTemplate) {
        Map<String, Integer> result = tableFieldsMapper.get(table);
        if (result != null) {
            return result;
        }

        // Double-Checked Locking
        synchronized (TableMetadata.class) {
            result = tableFieldsMapper.get(table);
            if (result != null) {
                return result;
            }
            result = jdbcTemplate.query("select * from " + table + " where 1 = 0",
                    rs -> {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int count = metaData.getColumnCount();
                        Map<String, Integer> l = new LinkedHashMap<>();
                        for (int i = 1; i <= count; i++) {
                            String fieldName = metaData.getColumnName(i);
                            int type = metaData.getColumnType(i);
                            l.put(fieldName, type);
                        }
                        return l;
                    });
            tableFieldsMapper.put(table, result);
        }
        return result;
    }

}
