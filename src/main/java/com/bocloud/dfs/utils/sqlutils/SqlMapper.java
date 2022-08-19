package com.bocloud.dfs.utils.sqlutils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class SqlMapper {
    private String sql;
    private List<Object> params = new ArrayList<>(32);

    public Object[] getParamsArray() {
        return getParams().toArray(new Object[params.size()]);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("sql=").append(sql).append(",value=[");
        for (int i = 0; i < params.size(); ++i) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(params.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    public void log() {
        log(this.sql, this.params);
    }

    public static void log(String sql, Object... params) {
        StringBuilder l = new StringBuilder(sql.length() * 2);
        int i = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?') {
                if (params.length <= i) {
                    l.append("?");
                } else {
                    l.append(paramToString(params[i++]));
                }
            } else {
                l.append(ch);
            }
        }
        log.info("SQL," + l.toString());
        //log.info("SQL,{};{}", mapper.getSql(), mapper.getParams());
    }

    public static void log(String sql, List<Object> params) {
        StringBuilder l = new StringBuilder(sql.length() * 2);
        int i = 0;
        for (char ch : sql.toCharArray()) {
            if (ch == '?') {
                if (params.size() <= i) {
                    l.append("?");
                } else {
                    l.append(paramToString(params.get(i++)));
                }
            } else {
                l.append(ch);
            }
        }
        log.info("SQL," + l.toString());
        //log.info("SQL,{};{}", mapper.getSql(), mapper.getParams());
    }

    private static String paramToString(Object o) {
        if (o instanceof String) {
            return "\'" + o.toString() + "\'";
        } else if (o instanceof LocalDateTime) {
            return "\'" + DateUtils.str((LocalDateTime) o) + "\'";
        } else if (o instanceof LocalDate) {
            return "\'" + DateUtils.str((LocalDate) o) + "\'";
        } else if (o instanceof LocalTime) {
            return "\'" + DateUtils.str((LocalTime) o) + "\'";
        } else if (o instanceof Instant) {
            return "\'" + DateUtils.str((Instant) o) + "\'";
        } else if (o instanceof java.util.Date) {
            return "\'" + DateUtils.str((java.util.Date) o) + "\'";
        } else {
            if (o == null) {
                return null;
            }
            return o.toString();
        }
    }
}
