package com.bocloud.dfs.component.internals;

import lombok.Builder;
import lombok.Data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LocalChannel {

  public void write(File file) throws IOException {
    try (FileOutputStream out = new FileOutputStream(file.path); FileChannel fileChannel = out.getChannel()) {
      file.byteBuffer.flip();
      fileChannel.write(file.byteBuffer);
    }
  }

  @Data
  @Builder
  static class File {
    private String path;
    private ByteBuffer byteBuffer;
  }
}
