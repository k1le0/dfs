package com.bocloud.dfs.component;


import com.bocloud.dfs.utils.Time;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class FSLockTest {

    @Autowired
    FSLock fsLock;

    @Test
    void tryLock() {
        fsLock.tryLock("test");
        System.out.println("1212");
        Time.SYSTEM.sleep(100000);
        fsLock.unlock();
    }
}