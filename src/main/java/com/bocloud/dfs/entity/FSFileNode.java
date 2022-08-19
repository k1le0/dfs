package com.bocloud.dfs.entity;

import com.bocloud.dfs.utils.BigDecimalSerializer2;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class FSFileNode extends BaseEntity {
    //主键ID
    private Integer id;
    //节点类型 1、文件夹；2文件
    private int Type;
    //文件大小
    @JsonSerialize(using = BigDecimalSerializer2.class)
    private BigDecimal fileSize;
    //标记是否还有子节点
    private int end;
    //当前节点所有父节点
    private Integer parentIds;
    //根节点为null 或者 0 数据库默认值为0
    private Integer parentId;

    private  int storageNodeId;
}
