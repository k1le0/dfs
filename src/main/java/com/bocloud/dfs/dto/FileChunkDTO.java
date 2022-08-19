package com.bocloud.dfs.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class FileChunkDTO {

    private String name;
    private BigDecimal size;
    private int order;
    private String md5;
    private MultipartFile file;
}
