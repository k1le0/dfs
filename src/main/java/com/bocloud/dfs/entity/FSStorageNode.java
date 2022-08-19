package com.bocloud.dfs.entity;


import com.bocloud.dfs.utils.BigDecimalSerializer2;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class FSStorageNode extends BaseEntity {

    private Integer id;
    /**
     * 1、win 2、linux
     */
    private int env;
    /**
     * 存储系统类型
     */
    private int fsType;

    private String ip;

    private int port;

    private String hostName;

    private int policy;

    private int status;

    @JsonSerialize(using = BigDecimalSerializer2.class)
    private BigDecimal spaceUsedSize;

    @JsonSerialize(using = BigDecimalSerializer2.class)
    private BigDecimal spaceTotalSize;

    private String storageVolumePath;


}
