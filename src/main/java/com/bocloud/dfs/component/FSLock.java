package com.bocloud.dfs.component;

import com.bocloud.dfs.repository.FSLockRepository;
import com.bocloud.dfs.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FSLock {


    private final FSLockRepository fsLockRepository;
    private final Map<Thread, LockInfo> locks;
    private final DelayQueue<LockInfo> lockQueue;
    private final static int LOCK_HEARTBEAT_INTERVAL = 10 * 3000;

    public FSLock(@Autowired FSLockRepository fsLockRepository) {
        this.fsLockRepository = fsLockRepository;
        this.lockQueue = new DelayQueue<>();
        this.locks = new ConcurrentHashMap<>();
        HeartbeatThread heartbeatThread = new HeartbeatThread(lockQueue);
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

    }

    @SuppressWarnings("all")
    public boolean tryLock(final String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        synchronized (key) {
            int lock = fsLockRepository.tryLock(key);
            if (lock <= 0) {
                return false;
            }
            locks.put(Thread.currentThread(), new LockInfo(lock, key));
            lockQueue.put(new LockInfo(LOCK_HEARTBEAT_INTERVAL, lock, key));
            return true;
        }
    }

    public boolean unlock() {
        LockInfo lockInfo = locks.get(Thread.currentThread());
        if (lockInfo != null && lockInfo.getId() > 0 && !StringUtils.isEmpty(lockInfo.getKey())) {
            return fsLockRepository.unlock(lockInfo.getId(), lockInfo.getKey());
        }
        return false;
    }

    @SuppressWarnings("all")
    private class LockInfo implements Delayed {
        private final int id;
        private final String key;
        private long heartbeatInterval;

        LockInfo(long heartbeatInterval, int id, String key) {
            this.heartbeatInterval = System.currentTimeMillis() + heartbeatInterval;
            this.id = id;
            this.key = key;
        }

        LockInfo(int id, String key) {
            this.id = id;
            this.key = key;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.heartbeatInterval - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        public int getId() {
            return id;
        }

        public String getKey() {
            return key;
        }
    }


    private class HeartbeatThread extends Thread {

        private final DelayQueue<LockInfo> lockQueue;

        public HeartbeatThread(DelayQueue<LockInfo> lockQueue) {
            this.lockQueue = lockQueue;
        }

        @Override
        @SuppressWarnings("all")
        public void run() {
            while (true) {
                LockInfo lockInfo = null;
                try {
                    lockInfo = this.lockQueue.take();
                    log.info("Lock renewal lockInfo={}", JsonUtils.toJson(lockInfo));
                    if (fsLockRepository.renew(lockInfo.getId())) {
                        lockQueue.put(new LockInfo(LOCK_HEARTBEAT_INTERVAL, lockInfo.getId(), lockInfo.getKey()));
                    }
                } catch (Exception e) {
                    assert lockInfo != null;
                    log.error("Lock renewal failed，key={},lockId={}", lockInfo.getKey(), lockInfo.getId(), e);
                }
            }
        }
    }
}
