package com.bocloud.dfs.repository;

import com.bocloud.dfs.entity.DFSNodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class FSNodeInfoRepositoryTest {

    @Autowired
    FSNodeInfoRepository FSNodeInfoRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register() {
        int id = FSNodeInfoRepository.register(DFSNodeInfo.builder().nodeId(1)
                .heartbeatTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build());
        System.out.println(id);
    }

    @Test
    void renew() {
        FSNodeInfoRepository.renew(3);
    }
}