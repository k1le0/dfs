package com.bocloud.dfs.component.internals;

import com.bocloud.dfs.component.DfsConfig;
import com.bocloud.dfs.entity.DFSNodeInfo;
import com.bocloud.dfs.repository.FSNodeInfoRepository;
import com.bocloud.dfs.utils.CopyOnWriteMap;
import com.bocloud.dfs.utils.JsonUtils;
import com.bocloud.dfs.utils.Time;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class Metadata {

    private final DfsConfig dfsConfig;


    private final ConcurrentMap<Integer, DFSNodeInfo> nodes;
    private final ConcurrentMap<Integer, Long> abnormalNodes;
    private final FSNodeInfoRepository fsNodeInfoRepository;
    private final Time time;

    public Metadata(DfsConfig dfsConfig, FSNodeInfoRepository fsNodeInfoRepository, Time time) {
        this.dfsConfig = dfsConfig;
        this.nodes = new CopyOnWriteMap<>();
        this.fsNodeInfoRepository = fsNodeInfoRepository;
        this.abnormalNodes = new ConcurrentHashMap<>();
        this.time = time;
        MetadataUpdateThread metadataUpdateThread = new MetadataUpdateThread(nodes, time);
        metadataUpdateThread.setName("metadataUpdateThread");
        metadataUpdateThread.setDaemon(true);
        metadataUpdateThread.start();
    }

    public DFSNodeInfo get(Integer node) {
        if (nodes.isEmpty()) {
            return null;
        }
        return nodes.get(node);
    }

    public long getAbnormalNodes(Integer nodeId) {
        return abnormalNodes.get(nodeId) == null ? 0 : abnormalNodes.get(nodeId);
    }

    public void addAbnormalNode(Integer nodeId) {
        abnormalNodes.putIfAbsent(nodeId, time.milliseconds());
    }

    public void delAbnormalNode(Integer nodeId) {
        abnormalNodes.remove(nodeId);
    }


    class MetadataUpdateThread extends Thread {

        final ConcurrentMap<Integer, DFSNodeInfo> nodes;
        private final Time time;

        public MetadataUpdateThread(ConcurrentMap<Integer, DFSNodeInfo> nodes, Time time) {
            this.nodes = nodes;
            this.time = time;
        }

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            for (; ; ) {
                try {
                    List<DFSNodeInfo> dfsNodeInfos = fsNodeInfoRepository.pull();
                    log.info("pull metadata nodes={}", JsonUtils.toJson(dfsNodeInfos));
                    if (dfsNodeInfos == null || dfsNodeInfos.isEmpty()) {
                        time.sleep(10000);
                        continue;
                    }
                    dfsNodeInfos.forEach(n -> nodes.put(n.getNodeId(), n));
                    time.sleep(dfsConfig.getMetadataPullInterval());
                } catch (Exception e) {
                    log.error("Failed to pull metadata");
                }
            }
        }


    }

}