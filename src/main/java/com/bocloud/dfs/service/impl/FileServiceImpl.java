package com.bocloud.dfs.service.impl;

import com.bocloud.dfs.component.FileManager;
import com.bocloud.dfs.dto.FSFileUploadTaskDTO;
import com.bocloud.dfs.dto.FileChunkDTO;
import com.bocloud.dfs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.channels.Channels;


@Service
public class FileServiceImpl implements FileService {


    private final FileManager fileManager;

    public FileServiceImpl(@Autowired FileManager fileManager) {
        this.fileManager = fileManager;
    }


    @Override
    public void uploadChunk(int taskId, FileChunkDTO fileChunkDTO) {
        try {
            fileManager.asyncUpload(taskId,
                    Channels.newChannel(fileChunkDTO.getFile().getInputStream()), fileChunkDTO.getSize(),
                    fileChunkDTO.getMd5(), fileChunkDTO.getName(),
                    fileChunkDTO.getOrder(), (dataChunk, e) -> {
                        if (e == null) {
                            System.out.println("写入本地成功");
                            //进行数据库写入
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upload(FSFileUploadTaskDTO fsFileUploadTaskDTO, InputStream in) {
        //1、创建上传文件任务
        String storageServiceType = "dfs";
        fileManager.syncUpload(storageServiceType, Channels.newChannel(in));
        //2、写入文件表

    }
}
