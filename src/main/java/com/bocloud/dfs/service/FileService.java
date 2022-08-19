package com.bocloud.dfs.service;

import com.bocloud.dfs.dto.FSFileUploadTaskDTO;
import com.bocloud.dfs.dto.FileChunkDTO;
import java.io.InputStream;


public interface FileService {

    void uploadChunk(int taskId, FileChunkDTO fileChunkDTO);

    void upload(FSFileUploadTaskDTO fsFileUploadTaskDTO, InputStream in);
}
