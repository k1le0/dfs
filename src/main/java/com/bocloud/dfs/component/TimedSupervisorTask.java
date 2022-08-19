package com.bocloud.dfs.component;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TimedSupervisorTask extends TimerTask {
    private final String name;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor executor;
    private final long timeoutMillis;
    private final Runnable task;
    private final AtomicLong delay;


    public TimedSupervisorTask(String name, ScheduledExecutorService scheduler, ThreadPoolExecutor executor,
                               int timeout, TimeUnit timeUnit, Runnable task) {
        this.name = name;
        this.scheduler = scheduler;
        this.executor = executor;
        this.timeoutMillis = timeUnit.toMillis(timeout);
        this.task = task;
        this.delay = new AtomicLong(timeoutMillis);
    }

    @Override
    public void run() {
        Future<?> future = null;
        try {
            future = executor.submit(task);
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);  // block until done or timeout
            delay.set(timeoutMillis);
        } catch (Throwable e) {
            if (executor.isShutdown() || scheduler.isShutdown()) {
                log.warn("task supervisor shutting down, can't accept the task");
            } else {
                log.warn("task supervisor threw an exception", e);
            }
        } finally {
            if (future != null) {
                future.cancel(true);
            }

            if (!scheduler.isShutdown()) {
                scheduler.schedule(this, delay.get(), TimeUnit.MILLISECONDS);
            }
        }

    }
}
