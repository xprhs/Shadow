package com.tencent.shadow.sample.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 任务执行者，线程池
 */
public class Worker {
    public Worker(){
        this.es = Executors.newCachedThreadPool();
        this.cs = new ExecutorCompletionService(this.es);
    }
    /**
     * 执行任务的线程池
     */
    private ExecutorService es;
    private ExecutorCompletionService cs;

    public ExecutorCompletionService getCs() {
        return cs;
    }

    public Future submitTask(Callable task){
        return this.cs.submit(task);
    }

    public Future take() throws InterruptedException{
        return this.cs.take();
    }

    public Future poll(){
        return this.cs.poll();
    }

    public Future poll(long timeout, TimeUnit unit) throws InterruptedException{
        return this.cs.poll(timeout,unit);
    }

    public void shutdown(){
        if(!this.es.isShutdown()){
            this.es.shutdown();
        }
    }

    public void shutdownnow(){
        if(!this.es.isShutdown()){
            this.es.shutdownNow();
        }
    }
}
