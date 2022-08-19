package com.bocloud.dfs.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class FSLockRepositoryTest {

    @Autowired
    FSLockRepository fsLockRepository;

    @Test
    void tryLock() {
        fsLockRepository.tryLock("SASAS", 3);
    }


    @Test
    void tryLock1() {
        System.out.println(fsLockRepository.tryLock("SASAS"));
    }

    @Test
    void unlock() {
        System.out.println(fsLockRepository.unlock(5, "SASAS"));
    }

    @Test
    void renew() {
        fsLockRepository.renew(7);
    }
}