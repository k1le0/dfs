package com.bocloud.dfs.component.internals;

import com.bocloud.dfs.component.*;
import com.bocloud.dfs.utils.Time;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class FileTemporaryStorageManager {

  private final LinkedBlockingQueue<DataChunk> queue;
  private final DoubleBufferQueue doubleBufferQueue;
  private final BufferPool bufferPool;
  private final String temporaryDirectoryRootPath;
  private final String link;
  private final Time time;
  private final DfsConfig dfsConfig;

  public FileTemporaryStorageManager(DfsConfig dfsConfig, Time time) {
    this.link = System.getProperty("os.name").toLowerCase().startsWith("win") ? "\\\\" : "/";
    this.temporaryDirectoryRootPath = dfsConfig.getTmpRoot();
    this.dfsConfig = dfsConfig;
    this.time = time;
    // root directory is initialized
    rootDirectoryInitialization(dfsConfig.getTmpRoot());
    // queue
    queue = new LinkedBlockingQueue<>();
    doubleBufferQueue = new DoubleBufferQueue();
    for (int i = 0; i < dfsConfig.getIoThreads(); i++) {
      // new TemporaryFileProcessor(new LocalChannel(), queue).start();
      TemporaryFileProcessor temporaryFileProcessor =
          new TemporaryFileProcessor(true, new LocalChannel(), doubleBufferQueue, queue);
      temporaryFileProcessor.setName("thread-" + i);
      temporaryFileProcessor.start();
    }
    this.bufferPool = new BufferPool(dfsConfig.getBufferSize(), dfsConfig.getBlockSize(), time);
  }

  private void rootDirectoryInitialization(String tmpRoot) {
    log.info("initialize the temporary directory，path={}", tmpRoot);
    if (StringUtils.isEmpty(tmpRoot))
      throw new DFSRootDirectoryInitException(
          "Failed to initialize the temporary directory for file upload");

    File tmp = new File(tmpRoot);
    if (!tmp.exists()) {
      if (!tmp.mkdirs()) {
        throw new DFSRootDirectoryInitException(
            "Failed to initialize the temporary directory for file upload");
      }
    }
  }

  @SuppressWarnings("all")
  public void cleanup(File file) {
    if (file == null) {
      return;
    } else if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) cleanup(f);
      }
      file.delete();
    } else {
      file.delete();
    }
  }

  public void write(
      int fileId,
      ReadableByteChannel readableByteChannel,
      BigDecimal size,
      String md5,
      String name,
      int order,
      Callback callback)
      throws InterruptedException, IOException {

    ByteBuffer buffer = bufferPool.allocate(dfsConfig.getAllocateBufferTimeoutMs());
    if (buffer == null) {
      throw new DFSOutOfMemoryBufferException("Out of memory buffer");
    }
    read(readableByteChannel, buffer);
    queue.put(
        DataChunk.builder()
            .fileId(fileId)
            .md5(md5)
            .size(size)
            .name(name)
            .order(order)
            .byteBuffer(buffer)
            .callback(callback)
            .build());
  }

  public void write(
      boolean clear,
      boolean isDouble,
      int fileId,
      ReadableByteChannel readableByteChannel,
      BigDecimal size,
      String md5,
      String name,
      int order,
      Callback callback)
      throws InterruptedException, IOException {

    ByteBuffer buffer = bufferPool.allocate(dfsConfig.getAllocateBufferTimeoutMs());
    if (buffer == null) {
      throw new DFSOutOfMemoryBufferException("Out of memory buffer");
    }
    read(readableByteChannel, buffer);
    if (isDouble) {
      doubleBufferQueue.put(
          DataChunk.builder()
              .fileId(fileId)
              .md5(md5)
              .size(size)
              .name(name)
              .order(order)
              .byteBuffer(buffer)
              .callback(callback)
              .build());
      doubleBufferQueue.setClear(clear);
    } else {
      queue.put(
          DataChunk.builder()
              .fileId(fileId)
              .md5(md5)
              .size(size)
              .name(name)
              .order(order)
              .byteBuffer(buffer)
              .callback(callback)
              .build());
    }
  }

  private void read(ReadableByteChannel readableByteChannel, ByteBuffer buffer) throws IOException {
    try {
      readableByteChannel.read(buffer);
    } finally {
      if (readableByteChannel != null) readableByteChannel.close();
    }
  }

  class TemporaryFileProcessor extends Thread {

    boolean isDouble;
    LocalChannel localChannel;
    DoubleBufferQueue doubleBufferQueue;
    LinkedBlockingQueue<DataChunk> dataChunks;

    public TemporaryFileProcessor(LocalChannel localChannel, LinkedBlockingQueue<DataChunk> queue) {
      this.localChannel = localChannel;
      this.dataChunks = queue;
    }

    public TemporaryFileProcessor(
        boolean isDouble,
        LocalChannel localChannel,
        final DoubleBufferQueue doubleBufferQueue,
        LinkedBlockingQueue<DataChunk> dataChunks) {
      this.isDouble = isDouble;
      this.localChannel = localChannel;
      this.doubleBufferQueue = doubleBufferQueue;
      this.dataChunks = dataChunks;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
      for (; ; ) {
        if (isDouble) {
          if (new BigDecimal(5 * 1429912).compareTo(doubleBufferQueue.getCurrSize()) < 1
              || doubleBufferQueue.getClear()) {
            LinkedBlockingQueue<DataChunk> chunks;
            synchronized (doubleBufferQueue) {
              if ('A' == doubleBufferQueue.getCurrQueue()) {
                chunks = doubleBufferQueue.getQueueA();
                doubleBufferQueue.setQueueA(new LinkedBlockingQueue<>());
                doubleBufferQueue.setCurrQueue('B');
              } else {
                chunks = doubleBufferQueue.getQueueB();
                doubleBufferQueue.setQueueB(new LinkedBlockingQueue<>());
                doubleBufferQueue.setCurrQueue('A');
              }
              doubleBufferQueue.setClear(false);
              doubleBufferQueue.setCurrSize(new BigDecimal(0));
            }
            for (DataChunk dataChunk : chunks) {
              try {
                //              dataChunk = doubleBufferQueue.getQueueA().take();
                // Create path
                dataChunk.setName(dataChunk.getName());
                createPath(dataChunk);
                // write to the local disk
                localChannel.write(
                    LocalChannel.File.builder()
                        .path(dataChunk.getPath())
                        .byteBuffer(dataChunk.getByteBuffer())
                        .build());

                // callback
                if (dataChunk.getCallback() != null)
                  dataChunk.getCallback().onCompletion(dataChunk, null);
              } catch (Exception e) {
                if (dataChunk != null && dataChunk.getCallback() != null)
                  try {
                    dataChunk.getCallback().onCompletion(dataChunk, e);
                  } catch (Exception p) {
                    log.error("callback is abnormal");
                  }
              } finally {
                if (dataChunk != null) bufferPool.deallocate(dataChunk.getByteBuffer());
              }
            }
          }
        } else {
          DataChunk dataChunk = null;
          try {
            dataChunk = this.dataChunks.take();
            // Create path
            createPath(dataChunk);
            // write to the local disk
            localChannel.write(
                LocalChannel.File.builder()
                    .path(dataChunk.getPath())
                    .byteBuffer(dataChunk.getByteBuffer())
                    .build());

            // callback
            if (dataChunk.getCallback() != null)
              dataChunk.getCallback().onCompletion(dataChunk, null);
          } catch (Exception e) {
            if (dataChunk != null && dataChunk.getCallback() != null)
              try {
                dataChunk.getCallback().onCompletion(dataChunk, e);
              } catch (Exception p) {
                log.error("callback is abnormal");
              }
          } finally {
            if (dataChunk != null) bufferPool.deallocate(dataChunk.getByteBuffer());
          }
        }
      }
    }

    private void createPath(DataChunk dataChunk) throws DFSDirectoryInitException {
      String path = temporaryDirectoryRootPath + link + dataChunk.getFileId();
      File tmp = new File(path);
      if (!tmp.exists()) {
        if (!tmp.mkdirs()) {
          throw new DFSDirectoryInitException("Failed to create a path=" + path + "");
        }
        log.info(
            "create a file data block folder,folder name is {},path={}",
            dataChunk.getFileId(),
            path);
      }
      dataChunk.setPath(path + link + dataChunk.getName());
    }
  }
}
