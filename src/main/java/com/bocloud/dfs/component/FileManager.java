package com.bocloud.dfs.component;

import com.bocloud.dfs.component.internals.Metadata;
import com.bocloud.dfs.component.internals.FileTemporaryStorageManager;
import com.bocloud.dfs.entity.DFSNodeInfo;
import com.bocloud.dfs.entity.FSFileDataChunk;
import com.bocloud.dfs.entity.FSFileUploadTask;
import com.bocloud.dfs.repository.FSFileDataChunkRepository;
import com.bocloud.dfs.repository.FSFileUploadTaskRepository;
import com.bocloud.dfs.repository.FSNodeInfoRepository;
import com.bocloud.dfs.utils.JsonUtils;
import com.bocloud.dfs.utils.Time;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件管理器
 */
@Component
@Slf4j
public class FileManager {
    /**
     * 元数据信息
     */
    private final Metadata metadata;
    private final DfsConfig dfsConfig;
    private final Time time;
    private final FileTemporaryStorageManager fileTemporaryStorageManager;


    public FileManager(@Autowired DfsConfig dfsConfig,
                       @Autowired FSNodeInfoRepository fsNodeInfoRepository,
                       @Autowired FSFileUploadTaskRepository fsFileUploadTaskRepository,
                       @Autowired FSLock fsLock,
                       @Autowired FSFileDataChunkRepository dataChunkRepository) {
        this.dfsConfig = dfsConfig;
        this.time = Time.SYSTEM;
        this.fileTemporaryStorageManager = new FileTemporaryStorageManager(this.dfsConfig, time);
        this.metadata = new Metadata(dfsConfig, fsNodeInfoRepository, time);

        for (int i = 0; i < dfsConfig.getNetworkThreads(); i++) {
            FileMergeThread fileMergeThread = new FileMergeThread(fsFileUploadTaskRepository, fsLock, dataChunkRepository);
            fileMergeThread.setDaemon(true);
            fileMergeThread.setName("fileMergeThread");
            fileMergeThread.start();
        }

    }


    public void syncUpload(String storageServiceType, ReadableByteChannel readableByteChannel) {
        Objects.requireNonNull(StorageServiceFactory.getStorageServiceClass(storageServiceType))
                .upload(readableByteChannel);
    }

    public void asyncUpload(int fileId, ReadableByteChannel readableByteChannel, BigDecimal size, String md5, String name, int order) throws IOException, InterruptedException {
        asyncUpload(fileId, readableByteChannel, size, md5, name, order, null);
    }

    public void asyncUpload(int fileId, ReadableByteChannel readableByteChannel, BigDecimal size, String md5, String name, int order, Callback callback) throws IOException, InterruptedException {
        fileTemporaryStorageManager.write(fileId, readableByteChannel, size, md5, name, order, callback);
    }


    class FileMergeThread extends Thread {

        final FSFileUploadTaskRepository fsFileUploadTaskRepository;
        final FSLock fsLock;
        final FSFileDataChunkRepository dataChunkRepository;


        public FileMergeThread(FSFileUploadTaskRepository fsFileUploadTaskRepository,
                               FSLock fsLock,
                               FSFileDataChunkRepository dataChunkRepository) {
            this.fsFileUploadTaskRepository = fsFileUploadTaskRepository;
            this.fsLock = fsLock;
            this.dataChunkRepository = dataChunkRepository;
        }

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            int cursor = 0;
            final int nodeId = dfsConfig.getNodeId();
            for (; ; ) {
                try {
                    List<FSFileUploadTask> fileUploadTasks = fsFileUploadTaskRepository.queryTasksToMerged(cursor);
                    if (fileUploadTasks == null || fileUploadTasks.isEmpty()) {
                        cursor = 0;
                        Time.SYSTEM.sleep(30000);
                        continue;
                    }

                    for (FSFileUploadTask fsFileUploadTask : fileUploadTasks) {
                        fsLock.tryLock("lock_" + nodeId + "_" + fsFileUploadTask.getId());

                        try {
                            List<FSFileDataChunk> fsFileDataChunks = dataChunkRepository.findByTaskId(fsFileUploadTask.getId());
                            if (fsFileDataChunks == null || fsFileDataChunks.isEmpty()) {
                                //todo 修改任务状态及文件数操作
                                System.out.println("文件块已经写完，任务状态异常，修复状态");
                                continue;
                            }
                            for (FSFileDataChunk dataChunk : fsFileDataChunks) {
                                if (dataChunk.getDfsNodeId() != nodeId) {
                                    //检查节点状态 todo
                                    DFSNodeInfo dfsNodeInfo = metadata.get(dataChunk.getDfsNodeId());
                                    if (dfsNodeInfo == null || dfsNodeInfo.getId() <= 0) {//节点异常
                                        synchronized ("node_" + dataChunk.getDfsNodeId()) {
                                            long abnormalTime = metadata.getAbnormalNodes(dataChunk.getDfsNodeId());
                                            if (abnormalTime > 0L && (time.milliseconds() - abnormalTime) / 1000 > 120L) {
                                                System.out.println("删除 nodeId=dataChunk.getDfsNodeId() 的数据块，告知前端从新上传 ,并且将任务状态修改为不可以合并");
                                                //todo 删除 nodeId=dataChunk.getDfsNodeId() 的数据块，告知前端从新上传 ,并且将任务状态修改为不可以合并
                                            } else {
                                                metadata.addAbnormalNode(dataChunk.getDfsNodeId());
                                                System.out.println("添加异常节点");
                                            }
                                        }
                                    } else {
                                        System.out.println("删除异常节点");
                                        metadata.delAbnormalNode(dfsNodeInfo.getNodeId());
                                    }
                                    //等待其它进程上传所持有的数据块
                                    break;
                                } else {
                                    //文件追加数据块 todo
                                    StorageServiceFactory.getStorageServiceClass("nfs").append();
                                }
                            }
                        } catch (Exception e) {
                            log.error("An exception occurred while merging file data blocks,taskId={}", fsFileUploadTask.getId(), e);
                        } finally {
                            fsLock.unlock();
                        }
                    }
                    cursor = fileUploadTasks.get(fileUploadTasks.size() - 1).getId();
                } catch (Exception e) {
                    log.error("merged task exception，cursor={}", cursor, e);
                }
            }
        }

    }


}
