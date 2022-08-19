package com.bocloud.dfs.entity;

import com.bocloud.dfs.utils.LocalDateTimeDeserializer;
import com.bocloud.dfs.utils.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.time.LocalDateTime;

@Data
@Builder
public class DFSNodeInfo {


    @Tolerate
    public DFSNodeInfo() {
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer nodeId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime heartbeatTime;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

}
