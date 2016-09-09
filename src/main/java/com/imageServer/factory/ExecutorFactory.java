package com.imageServer.factory;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 并发线程执行对象工厂方法
 * 单例
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */
public enum ExecutorFactory {

    EXECUTE;

    private static ExecutorService executorService;

    static {
        final int maxPoolSize = 200;
        //定义并发执行服务
        executorService = new ThreadPoolExecutor(5,maxPoolSize,0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t=new Thread(r);
                    t.setName("imageServerThreadPool");
                    return t;
                }
        );
    }
    /**
     * 获取执行类
     * @return
     */
    public ExecutorService getExecutor(){
        return executorService;
    }

}
