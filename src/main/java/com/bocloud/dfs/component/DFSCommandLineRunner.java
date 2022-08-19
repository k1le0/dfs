package com.bocloud.dfs.component;

import com.bocloud.dfs.entity.DFSNodeInfo;
import com.bocloud.dfs.repository.FSNodeInfoRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Component
public class DFSCommandLineRunner implements CommandLineRunner, Ordered {

    final ScheduledExecutorService scheduler;
    final ThreadPoolExecutor heartbeatExecutor;
    final FSNodeInfoRepository FSNodeInfoRepository;
    final DfsConfig dfsConfig;
    private TimedSupervisorTask heartbeatTask;


    public DFSCommandLineRunner(@Autowired FSNodeInfoRepository FSNodeInfoRepository,
                                @Autowired DfsConfig dfsConfig) {
        this.FSNodeInfoRepository = FSNodeInfoRepository;
        this.dfsConfig = dfsConfig;

        scheduler = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder()
                        .setNameFormat("DFSNodeClient-%d")
                        .setDaemon(true)
                        .build());


        heartbeatExecutor = new ThreadPoolExecutor(
                1, 2, 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("DFSNodeClient-HeartbeatExecutor-%d")
                        .setDaemon(true)
                        .build()

        );

    }


    @Override
    public void run(String... args) {

        int nodeId = FSNodeInfoRepository.register(
                DFSNodeInfo.builder()
                        .nodeId(dfsConfig.getNodeId())
                        .createTime(LocalDateTime.now())
                        .heartbeatTime(LocalDateTime.now())
                        .build());

        heartbeatTask = new TimedSupervisorTask(
                "heartbeat",
                scheduler,
                heartbeatExecutor,
                dfsConfig.getNodeRenewalIntervalInSecs(),
                TimeUnit.SECONDS,
                new HeartbeatThread(nodeId));

        scheduler.schedule(heartbeatTask,
                dfsConfig.getNodeRenewalIntervalInSecs(), TimeUnit.SECONDS);
    }


    private class HeartbeatThread implements Runnable {

        private final int nodeId;

        public HeartbeatThread(int nodeId) {
            this.nodeId = nodeId;
        }

        public void run() {
            FSNodeInfoRepository.renew(this.nodeId);
        }
    }


    @PreDestroy
    private void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }

        if (heartbeatTask != null) {
            heartbeatTask.cancel();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
