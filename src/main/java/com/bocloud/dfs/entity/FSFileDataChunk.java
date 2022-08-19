package com.bocloud.dfs.entity;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class FSFileDataChunk {
    //主键ID
    private Integer id;
    //数据块md5
    private String md5;
    private String name;
    //数据块大小
    private BigDecimal size;

    private int dfsNodeId;
    //序号
    private int order;
    //1，等待；2，成功；3，失败
    private int status;
    //任务ID
    private Integer taskId;

}
