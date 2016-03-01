package com.imageServer.service.impl;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/18 0018.
 */

import com.imageServer.config.FTPConnectAttr;
import com.imageServer.ftp.DownloadThread;
import com.imageServer.ftp.UploadThread;
import com.imageServer.service.FtpService;
import com.imageServer.util.ExecuteThreadUtil;
import com.imageServer.util.FTPUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * 图片服务器类
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/18 0018.
 */
@Service("ftpService")
public class FtpServiceImp implements FtpService {

    private Logger log = LoggerFactory.getLogger(FtpServiceImp.class);
    @Autowired
    private FTPConnectAttr ftpConnectAttr;

    /**
     * 上传文件到FTP服务器
     * 自动关闭输入流
     * @param inputStream 装载上传文件的输入流
     * @param directory   上传到远程FTP服务器的目录
     * @param fileName    存放在远程FTP服务器上的文件名
     * @return true ： 上传成功
     * @throws IOException 上传失败
     */
    @Override
    public boolean upload(InputStream inputStream, String directory, String fileName) throws IOException {
        log.info("进行FTP上传：" );
        log.info("目录：" + directory);
        log.info("文件名：" + fileName);
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream();
        pipedInputStream.connect(pipedOutputStream);
        ExecuteThreadUtil.execute(new UploadThread(inputStream, directory, fileName, pipedOutputStream,ftpConnectAttr));
        try {
            return pipedInputStream.read() == 1;
        } catch (IOException e) {
            return false;
        }
    }

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
    @Override
    public boolean download(File file, String path) throws IOException {
        log.info("进行FTP文件下载");
        log.info("下载文件：" + path);
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream();
        pipedInputStream.connect(pipedOutputStream);
        ExecuteThreadUtil.execute(new DownloadThread(file, path, pipedOutputStream,ftpConnectAttr));
        try {
            return pipedInputStream.read() == 1;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 删除FTP服务器上的指定文件
     *
     * @param directory 文件目录
     * @param fileName  文件名
     * @return true : 删除成功
     * @throws IOException 删除失败
     */
    synchronized public boolean deleteFile(String directory, String fileName) throws IOException {
        log.info("删除FTP文件");
        log.info("被删除的文件目录：" + directory);
        log.info("被删除的文件:" + fileName);
        FTPClient ftpClient = FTPUtil.getFTPClient(ftpConnectAttr);
        boolean result = ftpClient != null && ftpClient.deleteFile(directory + "/" + fileName);
        if(result && ftpClient.isConnected())
            ftpClient.disconnect();
        return result;
    }

    /**
     * 删除FTP服务器上的指定文件
     *
     * @param filePath 被删除文件在FTP服务器上的全路径
     * @return true : 删除成功
     * @throws IOException 删除失败
     */
    @Override
    synchronized public boolean deleteFile(String filePath) throws IOException {
        FTPClient ftpClient = FTPUtil.getFTPClient(ftpConnectAttr);
        boolean result = ftpClient != null && ftpClient.deleteFile(filePath);
        if(result && ftpClient.isConnected())
            ftpClient.disconnect();
        return result;
    }

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
    @Override
    public boolean replaceFile(InputStream upload, String delFilePath, String directory, String fileName) throws IOException {
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        pipedInputStream.connect(pipedOutputStream);
        ExecuteThreadUtil.execute(new UploadThread(upload, directory, fileName, pipedOutputStream,ftpConnectAttr));
        boolean result = pipedInputStream.read() == 1;
        if(delFilePath.equals(directory + "/" + fileName)){
            return result;
        }
        if(result)
            deleteFile(delFilePath);
        return result;
    }
}
