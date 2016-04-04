package com.imageServer.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */

import com.imageServer.factory.ExecutorFactory;
import org.apache.poi.ss.formula.functions.T;

import java.util.concurrent.*;

/**
 * 并发线程执行辅助工具类
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 15-1-23.
 */
public class ExecuteThreadUtil {

    private static ExecutorService executorService = ExecutorFactory.EXECUTE.getExecutor();

    /**
     * 执行一个无返回值的线程任务
     * 注：若执行的线程任务是长时间运行的线程，
     * 请不要用此线程池进行线程任务的创建，会有死锁的隐患
     * @param task
     */
    public static void execute(Runnable task){
        executorService.submit(task);
    }

    /**
     * 执行有返回值的线程任务
     * 超时100秒
     * 注：若执行的线程任务是长时间运行的线程，
     * 请不要用此线程池进行线程任务的创建，会有死锁的隐患
     * @param task
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static T execute(Callable<T> task) throws InterruptedException, ExecutionException, TimeoutException {
        Future<T> result = executorService.submit(task);
        return result.get(100, TimeUnit.SECONDS);
    }

}
