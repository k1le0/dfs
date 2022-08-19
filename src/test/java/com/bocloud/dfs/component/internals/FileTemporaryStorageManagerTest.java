package com.bocloud.dfs.component.internals;

import com.bocloud.dfs.component.DfsConfig;
import com.bocloud.dfs.utils.Time;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class FileTemporaryStorageManagerTest {

  @Test
  void write() throws InterruptedException {
    System.out.println("开始时间:" + LocalDateTime.now());
    DfsConfig config = new DfsConfig();
    config.setTmpRoot("C:\\uploadFiles\\tmp");
    config.setIoThreads(6);
    config.setBufferSize(31457280);
    config.setBlockSize(2097152);
    config.setAllocateBufferTimeoutMs(5000);
    FileTemporaryStorageManager fileTemporaryStorageManager =
        new FileTemporaryStorageManager(config, Time.SYSTEM);

    final File file = new File("C:\\uploadFiles\\test");
    final File[] files = file.listFiles();

    final AtomicLong t = new AtomicLong(0);
    for (int i = 0; i < 100; i++) {
      int finalI = i;
      new Thread(
              () -> {
                long start = System.currentTimeMillis();
                for (int j = 0; j < files.length; j++) {
                  try {
                    if (files.length - 1 == j) {
                      fileTemporaryStorageManager.write(
                          true,
                          true,
                          finalI + 1,
                          Files.newByteChannel(files[j].toPath()),
                          new BigDecimal(1429912),
                          "xxxx",
                          String.valueOf(j + 1),
                          j,
                          null);
                    } else {
                      fileTemporaryStorageManager.write(
                          false,
                          true,
                          finalI + 1,
                          Files.newByteChannel(files[j].toPath()),
                          new BigDecimal(1429912),
                          "xxxx",
                          String.valueOf(j + 1),
                          j,
                          null);
                    }
                    System.out.println("sasas" + (System.currentTimeMillis() - start));
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
                long hs = System.currentTimeMillis() - start;
                t.addAndGet(hs);
                System.out.println("线程" + finalI + "执行完成" + hs);
              })
          .start();
    }
    TimeUnit.SECONDS.sleep(5000);
    System.out.println("结束时间:" + LocalDateTime.now());
  }
}
