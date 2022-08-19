package com.bocloud.dfs.service.impl;

import com.bocloud.dfs.component.AbstractStorageService;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.channels.ReadableByteChannel;

@Component
public class NFSStorageServiceImpl extends AbstractStorageService {

    public NFSStorageServiceImpl() {
        super("nfs");
    }

    @Override
    public void upload(ReadableByteChannel readableByteChannel) {

    }

    @Override
    public void append() {
        //追加文件
        System.out.println("追加文件");
    }
}
