package com.bocloud.dfs.utils.sqlutils;

import java.time.*;
import java.time.format.DateTimeFormatter;

class DateUtils {
    public final static ZoneOffset TIME_ZONE = ZoneOffset.ofHours(8);
    private final static DateTimeFormatter datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(TIME_ZONE);
    private final static DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(TIME_ZONE);
    private final static DateTimeFormatter time = DateTimeFormatter.ofPattern(" HH:mm:ss").withZone(TIME_ZONE);

    public static String str(Instant i) {
        return datetime.format(i);
    }
    public static String str(LocalDate d) {
        return date.format(d.atStartOfDay().toInstant(TIME_ZONE));
    }
    public static String str(LocalDateTime d) {
        return datetime.format(d.toInstant(TIME_ZONE));
    }
    public static String str(LocalTime d) {
        return d.format(time);
    }
    public static String str(java.util.Date d) {
        return datetime.format(d.toInstant());
    }
}

