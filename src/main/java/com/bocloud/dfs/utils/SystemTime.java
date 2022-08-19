package com.bocloud.dfs.utils;

import java.util.concurrent.TimeUnit;

public class SystemTime implements Time {
    @Override
    public long milliseconds() {
        return System.currentTimeMillis();
    }

    @Override
    public long hiResClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(nanoseconds());
    }

    @Override
    public long nanoseconds() {
        return System.nanoTime();
    }

    @Override
    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // just wake up early
            Thread.currentThread().interrupt();
        }
    }
}
