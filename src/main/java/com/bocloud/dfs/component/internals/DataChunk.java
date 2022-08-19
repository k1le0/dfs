package com.bocloud.dfs.component.internals;

import com.bocloud.dfs.component.Callback;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

@Data
@Builder
public class DataChunk {

    private Integer fileId;
    private String path;
    private String name;
    private BigDecimal size;
    private ByteBuffer byteBuffer;
    private int order;
    private String md5;
    private Callback callback;

}
