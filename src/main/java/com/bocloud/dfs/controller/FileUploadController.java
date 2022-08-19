package com.bocloud.dfs.controller;

import com.bocloud.dfs.dto.FSFileUploadTaskDTO;
import com.bocloud.dfs.dto.FileChunkDTO;
import com.bocloud.dfs.service.FileService;
import com.bocloud.dfs.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;


@RestController
@RequestMapping("/file-upload")
public class FileUploadController {


    private final FileService fileService;

    public FileUploadController(@Autowired FileService fileService) {
        this.fileService = fileService;

    }

    @PostMapping("/uploadChunk/{taskId}")
    public Result<Object> uploadChunk(@PathVariable("taskId") Integer taskId,
                                      @ModelAttribute FileChunkDTO fileChunkDTO) {
        fileService.uploadChunk(taskId,fileChunkDTO);
        return Result.success();
    }

    @PostMapping("/create/uploadTask")
    public Result<Integer> uploadChunk(@RequestBody FSFileUploadTaskDTO uploadTaskDTO) {
        return Result.success(100);
    }





}
