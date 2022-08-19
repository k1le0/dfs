package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.FSFileDataChunk;
import com.bocloud.dfs.entity.FSFileUploadTask;
import com.bocloud.dfs.utils.sqlutils.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.*;

@Repository
public class FSFileDataChunkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static String TABLE_NAME = "dfs_file_data_chunk";

    public FSFileDataChunkRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FSFileDataChunk> findByTaskId(int taskId) {
        return SqlBuilder.select(TABLE_NAME)
                .where(
                        and(eq("`status`", 1),
                                eq("task_id", taskId)))
                .order("`order`")
                .limit(30)
                .queryForList(jdbcTemplate, FSFileDataChunk.class, null);
    }


}
