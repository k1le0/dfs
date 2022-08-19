package com.bocloud.dfs.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dfs")
@Data
public class DfsConfig {

  // node id不同,表明节点临时目录不同
  private int nodeId;
  // 续约间隔时间
  private int nodeRenewalIntervalInSecs;
  private int metadataPullInterval = 6000 * 10;
  // 数据块临时目录地址根
  private String tmpRoot;
  // 文件数据块内存buffer大小
  private long bufferSize = 31457280;
  private int blockSize = 2097152;
  // 本地io线程数
  private int ioThreads = 3;
  private int allocateBufferTimeoutMs = 5000;
  // 网络线程
  private int networkThreads = 3;
  //
  private int queueSize = 10 * 1024;
}
