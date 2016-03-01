package com.imageServer.context;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/29 0029.
 */

import com.imageServer.factory.ExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 关闭资源监听处理器
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/29 0029.
 */
public class ShutdownResources implements ServletContextListener {

    private static final  Logger log = LoggerFactory.getLogger(ShutdownResources.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.info("监听处理处理web容器关闭事件");

        log.info("关闭线程池");
        ExecutorFactory.getExecutor().shutdown();

    }
}
