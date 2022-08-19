package com.bocloud.dfs.component.internals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.LinkedBlockingQueue;


class LocalChannelTest {


    @Test
    void write() throws Exception {
        LinkedBlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>(10);

        final File file = new File("/Users/jimmy/Documents/test");
        final File[] files = file.listFiles();

        new Thread(() -> {
            assert files != null;
            for (File file1 : files) {
                RandomAccessFile randomAccessFile = null;
                FileChannel channel = null;
                try {
                    randomAccessFile = new RandomAccessFile(file1.getPath(), "r");
                    channel = randomAccessFile.getChannel();
                    ByteBuffer bf = ByteBuffer.allocate(1529912);
                    channel.read(bf);
                    if (!queue.offer(bf)) {
                        System.out.println("sassss");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    assert channel != null;
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(() -> {
            assert files != null;
            for (File file1 : files) {
                RandomAccessFile randomAccessFile = null;
                FileChannel channel = null;
                try {
                    randomAccessFile = new RandomAccessFile(file1.getPath(), "r");
                    channel = randomAccessFile.getChannel();
                    ByteBuffer bf = ByteBuffer.allocate(1529912);
                    channel.read(bf);
                    if (!queue.offer(bf)) {
                        System.out.println("sassss");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    assert channel != null;
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        new Thread(() -> {
            int i = 1;
            int time = 0;
            while (true) {
                ByteBuffer buffer = null;
                try {
                    buffer = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LocalChannel lc = new LocalChannel();
                LocalChannel.File localFile = LocalChannel.File
                        .builder()
                        .path("/Users/jimmy/Documents/test2/cc" + i + ".html")
                        .byteBuffer(buffer)
                        .build();
                try {
                    long start = System.currentTimeMillis();
                    lc.write(localFile);
                    time += System.currentTimeMillis() - start;
                    i++;
                    System.out.println(time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(10000000);
    }
}