package com.imageServer.ftp;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/2 0002.
 */

import com.imageServer.config.FTPConnectAttr;
import com.imageServer.util.FTPUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * FTP下载线程
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/2 0002.
 */
public class DownloadThread implements Runnable{

    private static final  Logger log = LoggerFactory.getLogger(DownloadThread.class);

    private File file;
    private String path;
    private PipedOutputStream pipedOutputStream;
    private FTPConnectAttr ftpConnectAttr;

    /**
     * 通道写出 0 : 下载失败
     * 通道写出1 ： 下载成功
     * @param file
     * 下载文件的存放对象
     * @param path
     * 需要下载的文件在FTP服务器上的全路径
     * @param pipedOutputStream
     * 下载结果通信通道
     *  @param ftpConnectAttr FTP连接配置
     */
    public DownloadThread(File file, String path, PipedOutputStream pipedOutputStream,FTPConnectAttr ftpConnectAttr) {
        this.file = file;
        this.path = path;
        this.pipedOutputStream = pipedOutputStream;
        this.ftpConnectAttr = ftpConnectAttr;
        log.info("download线程初始化完成,开始进行下载操作");
    }

    @Override
    public void run() {
        try {
            FTPClient ftpClient = FTPUtil.getFTPClient(ftpConnectAttr);
            if(ftpClient != null){
                //设置被动模式
                ftpClient.enterLocalPassiveMode();
                //设置以二进制方式传输
                ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);

                //检查远程文件是否存在
                FTPFile[] files = ftpClient.listFiles(new String(path.getBytes("GBK"), "iso-8859-1"));
                if (files.length != 1) {
                    log.warn("远程文件不存在:" + new String(path.getBytes("GBK"), "iso-8859-1"));
                    pipedOutputStream.write(0);
                    return;
                }

                long lRemoteSize = files[0].getSize();
                //本地存在文件，进行断点下载
                if (file.exists()) {
                    long localSize = file.length();
                    //判断本地文件大小是否大于远程文件大小
                    if (localSize >= lRemoteSize) {
                        log.warn("本地文件大于远程文件，下载中止");
                        pipedOutputStream.write(0);
                        return;
                    }

                    //进行断点续传，并记录状态
                    FileOutputStream out = new FileOutputStream(file, true);
                    ftpClient.setRestartOffset(localSize);
                    InputStream in = ftpClient.retrieveFileStream(new String(path.getBytes("GBK"), "iso-8859-1"));
                    byte[] bytes = new byte[1024];
                    long step = lRemoteSize / 100;
                    long process = localSize / step;
                    int c;
                    while ((c = in.read(bytes)) != -1) {
                        out.write(bytes, 0, c);
                        localSize += c;
                        long nowProcess = localSize / step;
                        if (nowProcess > process) {
                            process = nowProcess;
                            if (process % 10 == 0)
                                log.info("下载进度：" + process);
                        }
                    }
                    in.close();
                    out.close();
                    boolean isDo = ftpClient.completePendingCommand();
                    if (isDo) {
                        log.info("已下载，文件大小：" + file.length());
                        pipedOutputStream.write(1);
                    } else {
                        log.error("下载失败");
                        pipedOutputStream.write(0);
                    }
                } else {
                    OutputStream out = new FileOutputStream(file);
                    InputStream in = ftpClient.retrieveFileStream(new String(path.getBytes("GBK"), "iso-8859-1"));
                    byte[] bytes = new byte[1024];
                    long step = lRemoteSize / 100;
                    long process = 0;
                    long localSize = 0L;
                    int c;
                    while ((c = in.read(bytes)) != -1) {
                        out.write(bytes, 0, c);
                        localSize += c;
                        long nowProcess = localSize / step;
                        if (nowProcess > process) {
                            process = nowProcess;
                            if (process % 10 == 0)
                                log.info("下载进度：" + process);
                        }
                    }
                    in.close();
                    out.close();
                    boolean upNewStatus = ftpClient.completePendingCommand();
                    if (upNewStatus) {
                        log.info("已下载，文件大小：" + file.length());
                        pipedOutputStream.write(1);
                    } else {
                        log.error("下载失败");
                        pipedOutputStream.write(0);
                    }
                }
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            }
        } catch (IOException e) {
            log.error("下载失败IOException",e);
            e.printStackTrace();
        }
    }
}
