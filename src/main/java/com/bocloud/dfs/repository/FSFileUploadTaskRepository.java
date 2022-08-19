package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.FSFileUploadTask;
import com.bocloud.dfs.utils.sqlutils.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.*;

@Repository
public class FSFileUploadTaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static String TABLE_NAME = "dfs_file_upload_task";

    public FSFileUploadTaskRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<FSFileUploadTask> queryTasksToMerged(int id) {
        return SqlBuilder.select(TABLE_NAME)
                .where(
                        and(
                                gt("id", id),
                                eq("status", 5)
                        ))
                .order("id")
                .limit(30)
                .queryForList(jdbcTemplate, FSFileUploadTask.class, null);
    }


}
