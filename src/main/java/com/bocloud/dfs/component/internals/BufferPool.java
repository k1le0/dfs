package com.bocloud.dfs.component.internals;

import com.bocloud.dfs.utils.Time;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class BufferPool {

    private final ReentrantLock lock;
    /**
     * buffer pool totalMemory
     */
    private final long totalMemory;
    private final int blockSize;
    /**
     * buffer pool availableMemory
     */
    private long availableMemory;
    private final Deque<ByteBuffer> free;
    private final Time time;

    private final Deque<Condition> waiters;


    public BufferPool(long memory, int blockSize, Time time) {
        this.lock = new ReentrantLock();
        this.totalMemory = memory;
        this.availableMemory = memory;
        this.blockSize = blockSize;
        this.free = new ArrayDeque<>();
        this.waiters = new ArrayDeque<>();
        this.time = time;

    }

    public ByteBuffer allocate(long maxTimeToBlockMs) throws InterruptedException {
        log.info("The total size of the memory buffer is {}, and the size of the buffer block is {}", this.totalMemory, this.blockSize);
        this.lock.lock();
        try {
            if (!this.free.isEmpty())
                return this.free.pollFirst();

            if (this.availableMemory >= this.blockSize) {
                ByteBuffer allocatedBuffer = allocateByteBuffer(this.blockSize);
                this.availableMemory -= this.blockSize;
                return allocatedBuffer;
            } else {
                Condition moreMemory = this.lock.newCondition();
                ByteBuffer buffer = null;
                try {
                    long remainingTime = TimeUnit.MILLISECONDS.toNanos(maxTimeToBlockMs);
                    this.waiters.addLast(moreMemory);

                    for (; ; ) {
                        long startWaitNs = time.nanoseconds();
                        long timeNs;
                        boolean waitingTimeElapsed;
                        try {
                            waitingTimeElapsed = !moreMemory.await(remainingTime, TimeUnit.NANOSECONDS);
                        } finally {
                            long endWaitNs = time.nanoseconds();
                            timeNs = Math.max(0L, endWaitNs - startWaitNs);
                        }
                        if (waitingTimeElapsed) {
                            log.error("Failed to allocate memory within the configured max blocking time " + maxTimeToBlockMs + " ms.");
                            break;
                        }
                        remainingTime -= timeNs;

                        if (!this.free.isEmpty()) {
                            buffer = this.free.pollFirst();
                            break;
                        }
                    }
                    return buffer;
                } finally {
                    this.waiters.remove(moreMemory);
                }
            }

        } finally {
            if (!this.free.isEmpty() && !this.waiters.isEmpty())
                this.waiters.peekFirst().signal();
            this.lock.unlock();
        }
    }

    private ByteBuffer allocateByteBuffer(int size) {
        return ByteBuffer.allocate(size);
    }


    public void deallocate(ByteBuffer buffer) {
        lock.lock();
        try {
            buffer.clear();
            this.free.add(buffer);
            Condition moreMem = this.waiters.peekFirst();
            if (moreMem != null) moreMem.signal();
        } finally {
            lock.unlock();
        }
    }
}
