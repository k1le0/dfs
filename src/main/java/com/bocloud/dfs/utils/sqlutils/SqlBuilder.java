package com.bocloud.dfs.utils.sqlutils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SqlBuilder {
    public final static DbNull NULL = new DbNull();
    public final static DbDefault DEFAULT = new DbDefault();

    private SqlContext context = new SqlContext();
    private String from;
    private String select;
    private String selectAlias;
    private List<String[]> join;
    private String update;
    private String insert;
    private String delete;
    private String groupBy;
    private SqlCondition where;
    private Integer limitFrom;
    private Integer limitCount;
    private String order;
    private boolean forUpdate = false;
    private boolean replace = false;
    private boolean nolog = false;
    private boolean ignore = false;

    private SqlBuilder() {
    }

    public SqlBuilder clone() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        if (context != null) {
            sqlBuilder.context = new SqlContext(sqlBuilder.context);
        }
        sqlBuilder.from = from;
        sqlBuilder.select = select;
        sqlBuilder.selectAlias = selectAlias;
        if (join != null) {
            sqlBuilder.join = new ArrayList<>(join);
        }
        sqlBuilder.update = update;
        sqlBuilder.insert = insert;
        sqlBuilder.delete = delete;
        sqlBuilder.groupBy = groupBy;
        sqlBuilder.where = where;
        sqlBuilder.limitFrom = limitFrom;
        sqlBuilder.limitCount = limitCount;
        sqlBuilder.order = order;
        sqlBuilder.forUpdate = forUpdate;
        sqlBuilder.replace = replace;
        sqlBuilder.nolog = nolog;
        sqlBuilder.ignore = ignore;
        return sqlBuilder;
    }

    public static Map<String, Object> toMap(Object o) {
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBean(o);
        generator.setRequire(BeanMap.REQUIRE_GETTER);
        Map<String, Object> items = new HashMap<>();
        BeanMap bm = generator.create();
        for (Object key : bm.keySet()) {
            Object value = bm.get(key);
            if (value == null) {
                continue;
            }
            items.put(key + "", value);
        }
        return items;
    }

    public static Map<String, Object> toMapNotSkipNull(Object o) {
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        BeanMap.Generator generator = new BeanMap.Generator();
        generator.setBean(o);
        generator.setRequire(BeanMap.REQUIRE_GETTER);
        Map<String, Object> items = new HashMap<>();
        BeanMap bm = generator.create();
        for (Object key : bm.keySet()) {
            Object value = bm.get(key);
            if (value == null) {
                value = NULL;
            }
            items.put(key + "", value);
        }
        return items;
    }

    public static Map<String, Object> filter(String table, Map<String, Object> items, JdbcTemplate jdbcTemplate) {
        Map<String, Integer> fields = TableMetadata.getFields(table, jdbcTemplate);
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, Integer> field : fields.entrySet()) {
            String fieldName = field.getKey();
            if (!items.containsKey(fieldName)) {
                fieldName = Utils.toPropertyName(fieldName);
                if (!items.containsKey(fieldName)) {
                    continue;
                }
            }
            values.put(field.getKey(), items.get(fieldName));
        }
        return values;
    }

    public static Map<String, Object> saveFilter(String table, Map<String, Object> items, JdbcTemplate jdbcTemplate) {
        Map<String, Object> oldValues = filter(table, items, jdbcTemplate);
        Map<String, Object> newValues = new HashMap<>();
        if (!CollectionUtils.isEmpty(oldValues)) {
            oldValues.forEach((k, v) -> newValues.put("`" + k + "`", v));
        }
        return newValues;
    }

    private static String buildInsert(List<Map<String, Object>> itemss, SqlContext ctx) {
        Set<String> fields = new LinkedHashSet<>(itemss.size() + 5);
        itemss.forEach(x -> fields.addAll(x.keySet()));

        StringJoiner strFields = new StringJoiner(",", "(", ") values ");
        fields.forEach(strFields::add);

        StringJoiner strValues = new StringJoiner(",");
        itemss.forEach(items -> {
            StringJoiner strValue = new StringJoiner(" , ", "(", ")");
            fields.forEach(field -> {
                Object value = items.get(field);
                if (value == null) {
                    strValue.add(NULL.toSql(ctx));
                } else {
                    if (value instanceof Raw) {
                        strValue.add(((Raw) value).toSql(ctx));
                    } else {
                        strValue.add("{" + ctx.bind(value) + "}");
                    }
                }
            });
            strValues.add(strValue.toString());
        });
        return strFields.toString() + strValues.toString();
    }

    private static String buildInsert(Map<String, Object> items, SqlContext ctx) {
        return buildInsert(ImmutableList.of(items), ctx);
    }

    public static int execute(JdbcTemplate jdbcTemplate, String sql, Object... params) {
        SqlMapper.log(sql, params);
        return jdbcTemplate.update(sql, params);
    }

    public static void insertMulti(String table, List list, JdbcTemplate jdbcTemplate) {
        insertMulti(table, list, false, jdbcTemplate);
    }

    public static void insertMulti(String table, List list, boolean ignore, JdbcTemplate jdbcTemplate) {
        List<Map<String, Object>> itemss = new ArrayList<>(list.size());
        for (Object o : list) {
            itemss.add(filter(table, toMap(o), jdbcTemplate));
        }
        if (ignore) {
            SqlBuilder.insert(table, itemss).ignore().execute(jdbcTemplate);
        } else {
            SqlBuilder.insert(table, itemss).execute(jdbcTemplate);
        }
    }

    public static long save(String table, Object object, JdbcTemplate jdbcTemplate) {
        return SqlBuilder.insert(table, saveFilter(table, toMap(object), jdbcTemplate)).executeRetrunKey(jdbcTemplate);
    }

    public static int insert(String table, Object object, JdbcTemplate jdbcTemplate) {
        return SqlBuilder.insert(table, filter(table, toMap(object), jdbcTemplate)).execute(jdbcTemplate);
    }

    public static void insertIgnore(String table, Object object, JdbcTemplate jdbcTemplate) {
        SqlBuilder.insert(table, filter(table, toMap(object), jdbcTemplate)).ignore().execute(jdbcTemplate);
    }


    public static int replace(String table, Object object, JdbcTemplate jdbcTemplate) {
        return SqlBuilder.replace(table, filter(table, toMap(object), jdbcTemplate)).execute(jdbcTemplate);
    }

    public static long insertReturnId(String table, Object object, JdbcTemplate jdbcTemplate) {
        return SqlBuilder.insert(table, filter(table, toMap(object), jdbcTemplate)).executeRetrunKey(jdbcTemplate);
    }

    public static SqlBuilder insert(String table, List<Map<String, Object>> itemss) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.insert = buildInsert(itemss, sqlBuilder.context);
        return sqlBuilder;
    }

    public static SqlBuilder insert(String table, Map<String, Object> items) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.insert = buildInsert(items, sqlBuilder.context);
        return sqlBuilder;
    }

    public static SqlBuilder replace(String table, Map<String, Object> items) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.insert = buildInsert(items, sqlBuilder.context);
        sqlBuilder.replace = true;
        return sqlBuilder;
    }

    public static SqlBuilder update(String table, Map<String, Object> items) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;

        StringJoiner sb = new StringJoiner(",");
        for (Map.Entry<String, Object> item : items.entrySet()) {
            if (item.getValue() == null) {
                // skip, if update null, use NULL
            } else if (item.getValue() instanceof Raw) {
                sb.add(item.getKey() + "=" + ((Raw) item.getValue()).toSql(sqlBuilder.context));
            } else {
                sb.add(item.getKey() + "={" + sqlBuilder.context.bind(item.getValue()) + "}");
            }
        }
        sqlBuilder.update = sb.toString();
        return sqlBuilder;
    }


    public static int update(String table, Object object, SqlCondition condition, JdbcTemplate jdbcTemplate) {
        return SqlBuilder.update(table, filter(table, toMap(object), jdbcTemplate)).where(condition).execute(jdbcTemplate);
    }


    public static SqlBuilder delete(String table) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;

        sqlBuilder.delete = "";
        return sqlBuilder;
    }

    public static SqlBuilder select(String table) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.select = "";
        return sqlBuilder;
    }

    public static SqlBuilder select(String table, String alias) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.selectAlias = alias;
        sqlBuilder.select = "";
        return sqlBuilder;
    }

    public static SqlBuilder select(String table, Collection<String> fields) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;

        sqlBuilder.select = StringUtils.join(fields, ",");
        return sqlBuilder;
    }

    public static SqlBuilder select(String table, String[] fields) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.from = table;
        sqlBuilder.select = StringUtils.join(fields, ",");
        return sqlBuilder;
    }

    public SqlBuilder replace() {
        this.replace = true;
        return this;
    }

    public SqlBuilder forUpdate() {
        this.forUpdate = true;
        return this;
    }

    public SqlBuilder ignore() {
        this.ignore = true;
        return this;
    }

    public SqlBuilder selectFields(String fields) {
        this.select = fields;
        return this;
    }

    public SqlBuilder selectFields(String[] fields) {
        this.select = StringUtils.join(fields, ",");
        return this;
    }

    public SqlBuilder selectFields(Collection<String> fields) {
        this.select = StringUtils.join(fields, ",");
        return this;
    }

    public SqlBuilder autoSelectFields(Map<String, String> tables, JdbcTemplate jdbcTemplate) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> table : tables.entrySet()) {
            result.addAll(TableMetadata.getSelect(table.getKey(), table.getValue(), jdbcTemplate));
        }
        return selectFields(result);
    }

    public SqlBuilder autoSelectFields(JdbcTemplate jdbcTemplate) {
        List<String> result = TableMetadata.getSelect(from, selectAlias, jdbcTemplate);
        if (join != null) {
            for (String[] item : join) {
                result.addAll(TableMetadata.getSelect(item[1], item[2], jdbcTemplate));
            }
        }
        return selectFields(result);
    }

    public SqlBuilder nolog() {
        nolog = true;
        return this;
    }

    public SqlBuilder leftJoin(String table, String alias, String condition, Object... values) {
        if (join == null) {
            join = new ArrayList<>(5);
        }
        join.add(new String[]{"left", table, alias, new Raw(condition, values).toSql(context)});
        return this;
    }

    public SqlBuilder join(String table, String alias, String condition) {
        if (join == null) {
            join = new ArrayList<>(5);
        }
        join.add(new String[]{"inner", table, alias, condition});
        return this;
    }

    public SqlBuilder where(String condition, Object... params) {
        this.where = raw(condition, params);
        return this;
    }

    public SqlBuilder where(SqlCondition condition) {
        this.where = condition;
        return this;
    }

    public SqlBuilder limit(Integer count) {
        this.limitCount = count;
        return this;
    }

    public SqlBuilder order(String order) {
        this.order = order;
        return this;
    }

    public SqlBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public SqlBuilder limit(Integer from, Integer count) {
        this.limitFrom = from;
        this.limitCount = count;
        return this;
    }

    public SqlMapper toMapper() {
        StringBuilder sb = new StringBuilder(100);
        if (select != null) {
            sb.append("select ");
            sb.append(select);
            sb.append(" from ").append(from);
            if (selectAlias != null) {
                sb.append(" ").append(selectAlias);
            }
            if (join != null) {
                for (String[] item : join) {
                    sb.append(" ").append(item[0]).append(" join ").append(item[1]).append(" ").append(item[2]).append(" on ").append(item[3]);
                }
            }
            if (where != null) {
                sb.append(" where ").append(where.toSql(this.context));
            }
            if (groupBy != null) {
                sb.append(" group by ").append(groupBy);
            }
            if (order != null) {
                sb.append(" order by ").append(order);
            }
            if (limitCount != null) {
                if (limitFrom != null) {
                    // limit x,y
                    sb.append(" limit {").append(context.bind(limitFrom)).append("}");
                    sb.append(",{").append(context.bind(limitCount)).append("}");
                } else {
                    // limit x
                    sb.append(" limit {").append(context.bind(limitCount)).append("}");
                }
            }
            if (forUpdate) {
                sb.append(" for update");
            }
        } else if (update != null) {
            sb.append("update ").append(from).append(" set ").append(update);
            if (where != null) {
                sb.append(" where ").append(where.toSql(context));
            } else {
                throw new RuntimeException("update without where:sql=" + sb.toString());
            }
            if (limitCount != null) {
                sb.append(" limit ").append(limitCount);
            }
        } else if (delete != null) {
            sb.append("delete from ").append(from);
            if (where != null) {
                sb.append(" where ").append(where.toSql(context));
            } else {
                throw new RuntimeException("delete without where:sql=" + sb.toString());
            }
        } else if (insert != null) {
            if (replace) {
                sb.append("replace into ").append(from).append(" ").append(insert);
            } else if (ignore) {
                sb.append("insert ignore into ").append(from).append(" ").append(insert);
            } else {
                sb.append("insert into ").append(from).append(" ").append(insert);
            }
        } else {
            throw new RuntimeException("sqlbuilder is empty");
        }
        SqlMapper mapper = toMapper(sb.toString(), context.toMap());
        if (!nolog) {
            mapper.log();
        }
        return mapper;
    }

    public static SqlMapper toMapper(String sql, Map<String, Object> values) {
        SqlMapper mapper = new SqlMapper();

        StringBuilder sb = new StringBuilder(sql.length());
        StringBuilder param = null;
        int status = 0;

        for (int i = 0; i < sql.length(); ++i) {
            char ch = sql.charAt(i);
            if (status == 0) {
                if (ch == '{') {
                    status = 1;
                    param = new StringBuilder();
                } else {
                    sb.append(ch);
                }
            } else {
                if (ch == '}') {
                    String paramName = param.toString();
                    Object paramValue = values.get(paramName);
                    if (paramValue == null) {
                        sb.append("null");
                    } else if (paramValue instanceof Collection) {
                        Collection paramList = (Collection) paramValue;
                        if (paramList.isEmpty()) {
                            sb.append("null");
                        } else {
                            boolean first = true;
                            for (Object v : paramList) {
                                if (first) {
                                    sb.append("?");
                                    first = false;
                                } else {
                                    sb.append(",?");
                                }
                                mapper.getParams().add(v);
                            }
                        }
                    } else {
                        sb.append('?');
                        mapper.getParams().add(paramValue);
                    }
                    status = 0;
                    param = null;
                } else {
                    param.append(ch);
                }
            }
        }

        mapper.setSql(sb.toString());
        return mapper;
    }

    public static <T> SqlCondition skipEmpty(Collection<T> v, Function<Collection<T>, SqlCondition> action) {
        if (v == null || v.isEmpty()) {
            return null;
        }
        return action.apply(v);
    }

    public static SqlCondition skipEmpty(String v, Function<String, SqlCondition> action) {
        if (v == null || v.isEmpty()) {
            return null;
        }
        return action.apply(v);
    }

    public static <T> SqlCondition skipNull(T v, Function<T, SqlCondition> action) {
        if (v == null) {
            return null;
        }
        return action.apply(v);
    }

    public static And and(SqlCondition... conditions) {
        return new And(conditions);
    }

    public static Or or(SqlCondition... conditions) {
        return new Or(conditions);
    }

    public static Eq eq(String key, Object value) {
        return new Eq(key, value);
    }

    public static Not not(SqlCondition condition) {
        return new Not(condition);
    }

    public static Ge ge(String key, Object value) {
        return new Ge(key, value);
    }

    public static Gt gt(String key, Object value) {
        return new Gt(key, value);
    }

    public static Le le(String key, Object value) {
        return new Le(key, value);
    }

    public static Lt lt(String key, Object value) {
        return new Lt(key, value);
    }

    public static LikeAll likeAll(String key, Object value) {
        return new LikeAll(key, value);
    }

    public static LikeStart likeStart(String key, Object value) {
        return new LikeStart(key, value);
    }

    public static In in(String key, Collection value) {
        return new In(key, value);
    }

    public static Raw raw(String sql, Object... values) {
        return new Raw(sql, values);
    }

    public static Between between(String key, Object v1, Object v2) {
        return new Between(key, v1, v2);
    }

    public long queryForCount(JdbcTemplate jdbcTemplate) {
        selectFields("count(*) count");
        return queryForScalar(jdbcTemplate, Long.class);
    }

    public <T> T queryForScalar(JdbcTemplate jdbcTemplate, Class<T> clazz) {
        SqlMapper mapper = this.toMapper();
        try {
            return jdbcTemplate.queryForObject(mapper.getSql(), clazz, mapper.getParamsArray());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> queryForList(JdbcTemplate jdbcTemplate) {
        SqlMapper mapper = this.toMapper();
        return jdbcTemplate.queryForList(mapper.getSql(), mapper.getParamsArray());
    }

    public List<Map<String, Object>> queryForList(JdbcTemplate jdbcTemplate, Map<String, Class<?>> classMap) {
        return queryForList(jdbcTemplate, classMap, null);
    }

    public <T> T queryForObject(JdbcTemplate jdbcTemplate, Class<T> clazz, RowFunc rowFunc) {
        List<T> list = queryForList(jdbcTemplate, ImmutableMap.of("*", clazz), rowFunc)
                .stream()
                .map(x -> (T) x.get("*"))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public <T> List<T> queryForList(JdbcTemplate jdbcTemplate, Class<T> clazz) {
        return queryForList(jdbcTemplate, clazz, null);
    }

    public <T> List<T> queryForList(JdbcTemplate jdbcTemplate, Class<T> clazz, RowFunc rowFunc) {
        if (clazz == Long.class || clazz == Integer.class || clazz == String.class || clazz == BigDecimal.class || clazz == LocalDate.class || clazz == LocalDateTime.class) {
            SqlMapper mapper = this.toMapper();
            return jdbcTemplate.queryForList(mapper.getSql(), mapper.getParamsArray(), clazz);
        }
        return queryForList(jdbcTemplate, ImmutableMap.of("*", clazz), rowFunc)
                .stream()
                .map(x -> (T) x.get("*"))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> queryForList(JdbcTemplate jdbcTemplate, Map<String, Class<?>> classMap, RowFunc rowFunc) {
        if (select == null || select.isEmpty()) {
            autoSelectFields(jdbcTemplate);
        }
        List<Map<String, Object>> results = new ArrayList<>(100);
        SqlMapper mapper = this.toMapper();
        ObjectMapper objectMapper = new ObjectMapper();
        final DataRow[] dr = new DataRow[1];
        jdbcTemplate.query(mapper.getSql(), (rs, rowNum) -> {
            Map<String, Object> objectMap = new HashMap<>();
            for (Map.Entry<String, Class<?>> entry : classMap.entrySet()) {
                try {
                    Object o = entry.getValue().newInstance();
                    for (String k : StringUtils.split(entry.getKey(), ',')) {
                        objectMap.put(k, o);
                    }
                } catch (Exception e) {
                    log.error("failed to newInstance for class,{}", entry.getValue().getName());
                    throw new RuntimeException(e);
                }
            }
            objectMapper.mapper(objectMap, rs);
            if (rowFunc != null) {
                if (dr[0] == null) {
                    dr[0] = new DataRow(rs);
                }
                rowFunc.onRow(objectMap, dr[0]);
            }
            results.add(objectMap);
            return null;
        }, mapper.getParamsArray());
        return results;
    }

    public long executeRetrunKey(JdbcTemplate jdbcTemplate) {
        SqlMapper mapper = this.toMapper();

        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(mapper.getSql(), Statement.RETURN_GENERATED_KEYS);
            int pos = 1;
            for (Object o : mapper.getParams()) {
                statement.setObject(pos, o);
                pos++;
            }
            return statement;
        }, holder);
        return holder.getKey().longValue();
    }

    public int execute(JdbcTemplate jdbcTemplate) {
        SqlMapper mapper = this.toMapper();
        return jdbcTemplate.update(mapper.getSql(), mapper.getParamsArray());
    }
}
