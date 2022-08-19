package com.bocloud.dfs.repository;

import com.bocloud.dfs.component.DfsConfig;
import com.bocloud.dfs.component.FSLock;
import com.bocloud.dfs.entity.FSFileDataChunk;
import com.bocloud.dfs.entity.FSFileUploadTask;
import com.bocloud.dfs.utils.JsonUtils;
import com.bocloud.dfs.utils.Time;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class FSFileUploadTaskRepositoryTest {

    @Autowired
    FSFileUploadTaskRepository fsFileUploadTaskRepository;
    @Autowired
    FSFileDataChunkRepository dataChunkRepository;

    @Autowired
    DfsConfig dfsConfig;
    @Autowired
    FSLock fsLock;

    @Test
    void queryTasksToMerged() {

        //
        new Thread(() -> {
            int cursor = 0;
            final int nodeId = 2;
            for (; ; ) {
                List<FSFileUploadTask> fsFileUploadTasks = fsFileUploadTaskRepository.queryTasksToMerged(cursor);
                if (fsFileUploadTasks.isEmpty()) {
                    cursor = 0;
                    Time.SYSTEM.sleep(30000);
                    continue;
                }


                fsFileUploadTasks.forEach(n -> {
                    fsLock.tryLock(nodeId + "_" + n.getId());
                    List<FSFileDataChunk> fsFileDataChunks = dataChunkRepository.findByTaskId(n.getId());

                    for (FSFileDataChunk dataChunk : fsFileDataChunks) {
                        if (dataChunk.getDfsNodeId() != nodeId) {
                            //check node
                            //task 飞
                            System.out.println("------------------------客户端2 跳过");
                            break;
                        } else {
                            System.out.println("客户端2写io");
                        }
                    }
                    fsLock.unlock();
                });
                cursor = fsFileUploadTasks.get(fsFileUploadTasks.size() - 1).getId();
            }
        }).start();


        Time.SYSTEM.sleep(20000);

        new Thread(() -> {
            int cursor = 0;
            final int nodeId = 1;
            for (; ; ) {
                List<FSFileUploadTask> fsFileUploadTasks = fsFileUploadTaskRepository.queryTasksToMerged(cursor);
                if (fsFileUploadTasks.isEmpty()) {
                    cursor = 0;
                    Time.SYSTEM.sleep(3000);
                    continue;
                }


                fsFileUploadTasks.forEach(n -> {
                    fsLock.tryLock(nodeId + "_" + n.getId());
                    List<FSFileDataChunk> fsFileDataChunks = dataChunkRepository.findByTaskId(n.getId());
                    for (FSFileDataChunk dataChunk : fsFileDataChunks) {
                        if (dataChunk.getDfsNodeId() != nodeId) {
                            //x
                            System.out.println("------------------------客户端1 跳过");
                            break;
                        } else {
                            System.out.println("客户端1写io");
                        }
                    }
                    fsLock.unlock();
                });
                cursor = fsFileUploadTasks.get(fsFileUploadTasks.size() - 1).getId();
            }
        }).start();


        Time.SYSTEM.sleep(111000000);
    }

}