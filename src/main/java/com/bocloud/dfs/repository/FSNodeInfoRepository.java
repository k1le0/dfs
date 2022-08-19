package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.DFSNodeInfo;
import com.bocloud.dfs.utils.Time;
import com.bocloud.dfs.utils.sqlutils.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Random;

import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.and;
import static com.bocloud.dfs.utils.sqlutils.SqlBuilder.eq;

@Repository
public class FSNodeInfoRepository {


    private final JdbcTemplate jdbcTemplate;
    private final static String TABLE_NAME = "dfs_node";

    public FSNodeInfoRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public int register(DFSNodeInfo dfsNodeInfo) {
        DFSNodeInfo node = SqlBuilder.select(TABLE_NAME)
                .where(and(
                        eq("node_id", dfsNodeInfo.getNodeId())))
                .queryForObject(jdbcTemplate, DFSNodeInfo.class, null);
        if (node == null || node.getId() <= 0) {
            try {
                return (int) SqlBuilder.insertReturnId(TABLE_NAME, dfsNodeInfo, jdbcTemplate);
            } catch (Exception e) {
                Time.SYSTEM.sleep(new Random().nextInt(2000));
                return register(dfsNodeInfo);
            }
        } else {
            return node.getId();
        }
    }

    public void renew(int id) {
        SqlBuilder.execute(jdbcTemplate, "update dfs_node set heartbeat_time=now() where id=? ", id);
    }


    public List<DFSNodeInfo> pull() {
        return SqlBuilder.select(TABLE_NAME).queryForList(jdbcTemplate, DFSNodeInfo.class, null);
    }
}
