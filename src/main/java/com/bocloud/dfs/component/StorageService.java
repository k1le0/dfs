package com.bocloud.dfs.component;


import java.nio.channels.ReadableByteChannel;

public interface StorageService {


    void upload(ReadableByteChannel readableByteChannel);

    void append();
}
