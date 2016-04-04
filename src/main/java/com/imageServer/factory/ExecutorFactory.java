package com.imageServer.factory;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 并发线程执行对象工厂方法
 * 单例
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */
public enum ExecutorFactory {

    EXECUTE;

    private ExecutorService executorService;

    ExecutorFactory(){
        if(executorService == null){
            final int cpuCore = Runtime.getRuntime().availableProcessors();
            //线程数（当前CPU数+1）
            final int poolSize = cpuCore+1;
            //定义并发执行服务
            executorService = Executors.newFixedThreadPool(poolSize);

        }
    }
    /**
     * 获取执行类
     * @return
     */
    public ExecutorService getExecutor(){
        if(executorService == null){
            final int cpuCore = Runtime.getRuntime().availableProcessors();
            //线程数（当前CPU数+1）
            final int poolSize = cpuCore+1;
            //定义并发执行服务
            executorService = Executors.newFixedThreadPool(poolSize);

        }
        return executorService;
    }

}
