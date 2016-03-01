package com.imageServer.service;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */

import javax.servlet.http.HttpServletRequest;

/**
 * FTP服务器上的图片进行缓存服务
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */
public interface FtpImageCacheService {

    boolean imageToCache(HttpServletRequest request);

    /**
     * 计算获取缓存文件的hash文件名
     * @param request
     * @return
     */
    String getCacheFileHashName(HttpServletRequest request);

    /**
     * 计算获取缓存文件的绝对路径位置
     * @param request
     * @return
     */
    String getCacheFileAbsPath(HttpServletRequest request);

    /**
     * 计算获取缓存文件的目录绝对路径位置
     * @param request
     * @return
     */
    String getCacheFileDirPath(HttpServletRequest request);

}
