package com.bocloud.dfs.dto;

import com.bocloud.dfs.utils.BigDecimalSerializer2;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FSFileUploadTaskDTO {


    //文件名
    private String fileName;
    //上传路径
    private String path;
    //状态 1、传输完成（以成功上传至存储节点）；2、失败；3、上传中；4、排队中（用户维度并发度限制）
    private int status;
    //数据块数量
    private int chunkSize;
    //需要上传至某一个文件下的节点id
    private Integer nodeId;
    private Integer namespaceId;
    //存储节点ID
    private Integer storageNodeId;
    //文件大小
    @JsonSerialize(using = BigDecimalSerializer2.class)
    private BigDecimal fileSize;
}
