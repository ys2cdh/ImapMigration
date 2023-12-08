package com.funnysalt.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActiveTasksThreadPool extends ThreadPoolExecutor {

    private final ConcurrentHashMap<Runnable, Boolean> activeTasks = new ConcurrentHashMap<>();
    public ActiveTasksThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {

        activeTasks.put(r, Boolean.TRUE);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {

        super.afterExecute(r, t);
        activeTasks.remove(r);
    }

}
