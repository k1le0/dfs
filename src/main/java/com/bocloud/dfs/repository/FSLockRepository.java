package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.FSLock;
import com.bocloud.dfs.utils.sqlutils.SqlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.and;
import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.eq;

@Repository
@Slf4j
public class FSLockRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static String TABLE_NAME = "dfs_lock";

    public FSLockRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int tryLock(String key, Integer expire) {
        log.info("tryLock key={},expireTime={}", key, expire);
        if (StringUtils.isEmpty(key) || expire == null || expire <= 0) {
            return 0;
        }
        try {
            return (int) SqlBuilder.save(TABLE_NAME,
                    FSLock.builder()
                            .key(key)
                            .heartbeatTime(LocalDateTime.now())
                            .expireTime(LocalDateTime.now().plusSeconds(expire))
                            .createTime(LocalDateTime.now())
                            .build(),
                    jdbcTemplate);
        } catch (DuplicateKeyException d) {
            log.warn("Lock already exists，key={}", key);
        } catch (Exception e) {
            log.error("Locking failure，key={}", key);
        }
        return 0;
    }


    public boolean renew(int id) {
        return SqlBuilder.execute(jdbcTemplate, "update dfs_lock set heartbeat_time=? where id=?", LocalDateTime.now(), id) > 0;
    }

    public int tryLock(String key) {
        log.info("tryLock key={}", key);
        if (StringUtils.isEmpty(key)) {
            return 0;
        }
        try {
            return (int) SqlBuilder.save(TABLE_NAME,
                    FSLock.builder()
                            .key(key)
                            .heartbeatTime(LocalDateTime.now())
                            .createTime(LocalDateTime.now())
                            .build(),
                    jdbcTemplate);
        } catch (DuplicateKeyException d) {
            log.warn("Lock already exists，key={}", key);
        } catch (Exception e) {
            log.error("Locking failure，key={}", key);
        }
        return 0;
    }


    public boolean unlock(int id, String key) {
        log.info("unlock key={},id={}", key, id);
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            SqlBuilder.delete(TABLE_NAME)
                    .where(
                            and(
                                    eq("id", id),
                                    eq("`key`", key)))
                    .execute(jdbcTemplate);
            return true;
        } catch (Exception e) {
            log.error("unlock failure，id={},key={}", id, key, e);
            return false;
        }
    }
}
