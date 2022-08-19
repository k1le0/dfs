package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.FSFileDataChunk;
import com.bocloud.dfs.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class FSFileDataChunkRepositoryTest {

    @Autowired
    FSFileDataChunkRepository dataChunkRepository;

    @Test
    void findByTaskId() {
        List<FSFileDataChunk> fsFileDataChunks= dataChunkRepository.findByTaskId(1);
        System.out.println(JsonUtils.toJson(fsFileDataChunks));
    }
}