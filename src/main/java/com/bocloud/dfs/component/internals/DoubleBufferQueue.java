package com.bocloud.dfs.component.internals;

import lombok.Data;

import java.math.BigDecimal;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author YuHan Wang
 * @date 2021/11/25 22:32
 */
@Data
public class DoubleBufferQueue {
  private LinkedBlockingQueue<DataChunk> queueA = new LinkedBlockingQueue<>();
  private LinkedBlockingQueue<DataChunk> queueB = new LinkedBlockingQueue<>();
  private char currQueue = 'A';
  private BigDecimal currSize = new BigDecimal(0);
  private boolean clear = false;

  public void put(DataChunk dataChunk) {
    synchronized (this) {
      if ('A' == currQueue) {
        queueA.add(dataChunk);
      } else {
        queueB.add(dataChunk);
      }
      currSize = currSize.add(dataChunk.getSize());
    }
  }

  public boolean getClear() {
    return clear;
  }
}
