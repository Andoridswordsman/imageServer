package com.imageServer.service;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/18 0018.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * FTP服务类
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/18 0018.
 */
public interface FtpService {

    /**
     * 上传文件到FTP服务器
     * 自动关闭输入流
     * @param inputStream 装载上传文件的输入流
     * @param directory   上传到远程FTP服务器的目录
     * @param fileName    存放在远程FTP服务器上的文件名
     * @return true ： 上传成功
     * @throws IOException 上传失败
     */
    public boolean upload(InputStream inputStream, String directory, String fileName) throws IOException;

    /**
     * 从FTP服务器下载文件
     * @param file
     * 下载文件的存放对象
     * @param path
     * 需要下载的文件在FTP服务器上的全路径
     * @return
     * true:下载成功
     * @throws IOException
     * 下载失败
     */
    public boolean download(File file, String path)throws IOException;

    /**
     * 删除FTP服务器上的指定文件
     * @param directory
     * 文件目录
     * @param fileName
     * 文件名
     * @return
     * true : 删除成功
     * @throws IOException
     * 删除失败
     */
    public boolean deleteFile(String directory, String fileName)throws IOException;

    /**
     * 删除FTP服务器上的指定文件
     * @param filePath
     * 被删除文件在FTP服务器上的全路径
     * @return
     * true : 删除成功
     * @throws IOException
     * 删除失败
     */
    public boolean deleteFile(String filePath) throws IOException;

    /**
     * 替换FTP服务器上的文件
     * 自动识别上传文件与删除文件是同一全路径的情况
     * @param upload      需要上传的文件
     * @param delFilePath FTP服务器上需要被删除的文件的全路径
     * @param directory   上传的目录
     * @param fileName    存放的文件名
     * @return true ： 替换成功
     * @throws IOException
     */
    public boolean replaceFile(InputStream upload, String delFilePath, String directory, String fileName)throws IOException;
}
