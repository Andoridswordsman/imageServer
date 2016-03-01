package com.imageServer.ftp;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/2 0002.
 */

import com.imageServer.config.FTPConnectAttr;
import com.imageServer.util.FTPUtil;
import com.imageServer.util.FileUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FTP上传线程
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/2 0002.
 */
public class UploadThread implements Runnable{

    private static final  Logger log = LoggerFactory.getLogger(UploadThread.class);

    private InputStream inputStream;
    private String directory;
    private String fileName;
    private PipedOutputStream pipedOutputStream;
    private boolean export;
    private FTPConnectAttr ftpConnectAttr;

    /**
     * 注：此构造方法
     * 通道写出 0 : 上传失败
     * 通道写出1 ： 上传成功
     *
     * @param inputStream 装载上传文件的输入流
     * @param directory   上传到远程FTP服务器的目录
     * @param fileName    存放在远程FTP服务器上的文件名
     * @param pipedOutputStream 上传结果通信通道
     * @param ftpConnectAttr FTP连接配置
     */
    public UploadThread(InputStream inputStream, String directory, String fileName, PipedOutputStream pipedOutputStream,FTPConnectAttr ftpConnectAttr) {
        this.inputStream = inputStream;
        this.directory = directory;
        this.fileName = fileName;
        this.pipedOutputStream = pipedOutputStream;
        this.export  = false;
        this.ftpConnectAttr = ftpConnectAttr;
        log.info("upload线程初始化完成，开始进行FTP线程上传操作");
    }

    /**
     * 注：此构造方法
     * 通道写出上传进度百分比数值(int)
     *
     * @param pipedOutputStream
     * 输出的连接管道流
     * @param inputStream
     * 装载上传文件的输入流
     * @param directory
     * 上传到远程FTP服务器的目录
     * @param fileName
     * 存放在远程FTP服务器上的文件名
     */
    public UploadThread(PipedOutputStream pipedOutputStream,InputStream inputStream, String directory, String fileName) {
        this.inputStream = inputStream;
        this.directory = directory;
        this.fileName = fileName;
        this.pipedOutputStream = pipedOutputStream;
        this.export = true;
        log.info("upload线程初始化完成，开始进行FTP线程上传操作");
    }

    @Override
    public void run() {
        File tempFile = null;
        FTPClient ftpClient = null;
        try {
            ftpClient = FTPUtil.getFTPClient(ftpConnectAttr);
            if(ftpClient == null){
                log.error("FTPClient获取失败，上传失败");
                pipedOutputStream.write(0);
                return;
            }

            //设置上传目录,并自动创建目录
            if(!ftpClient.changeWorkingDirectory(directory)){
                log.info("上传目录不存在，开始自动创建目录");
                String[] dirs = directory.split("/");
                List<String> dirList = new ArrayList<>();
                for (String dir : dirs) {
                    if(!dir.equals("")){
                        if(dirList.size() == 0){
                            dirList.add("/" + dir);
                        }else{
                            dirList.add(dirList.get(dirList.size()-1) + "/" + dir);
                        }
                    }
                }
                for (String s : dirList) {
                    if(!ftpClient.changeWorkingDirectory(s)){
                        if(!ftpClient.makeDirectory(s)){
                            log.error("目录创建失败，上传失败");
                            pipedOutputStream.write(0);
                            return;
                        }
                    }
                }
                if(ftpClient.changeWorkingDirectory(directory)){
                    log.info("目录创建成功，目标路径更改成功：" + directory);
                }
            }
            log.info("目标路径更改成功：" + directory);

            //设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            //设置以二进制流的方式传输
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("GBK");
            if(directory.lastIndexOf("/") == -1){
                directory  = directory + "/";
            }
            String remoteFileName = directory + "/" + fileName;
            boolean result;
            //检查远程是否存在文件
            FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
            /* 创建临时文件 */
            tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + fileName);
            if(!FileUtil.inputStreamToFile(inputStream, tempFile)){
                log.error("上传的文件流临时文件创建失败,上传失败");
                pipedOutputStream.write(0);
                return ;
            }
            if (files.length == 1) {
                long remoteSize = files[0].getSize();
                long localSize = tempFile.length();
                if (remoteSize == localSize) {
                    log.warn("文件已存在");
                    pipedOutputStream.write(1);
                    return ;
                } else if (remoteSize > localSize) {
                    log.warn("远程文件大于本地文件");
                    pipedOutputStream.write(0);
                    return ;
                }

                //尝试移动文件内读取指针,实现断点续传
                if(export){
                    result = uploadFile(pipedOutputStream,remoteFileName, tempFile, ftpClient, remoteSize);
                }else {
                    result = uploadFile(remoteFileName, tempFile, ftpClient, remoteSize);
                }

                //如果断点续传没有成功，则删除服务器上文件，重新上传
                if (!result) {
                    if (!ftpClient.deleteFile(remoteFileName)) {
                        log.warn("断点续传没有成功，删除服务器上文件，进行重新上传");
                    }
                    log.info("重新上传文件");
                    if(export){
                        result = uploadFile(pipedOutputStream,remoteFileName, tempFile, ftpClient, 0);
                    }else {
                        result = uploadFile(remoteFileName, tempFile, ftpClient, 0);
                    }
                }
            } else {
                if(export){
                    result = uploadFile(pipedOutputStream,remoteFileName, tempFile, ftpClient, 0);
                }else {
                    result = uploadFile(remoteFileName, tempFile, ftpClient, 0);
                }
            }

            if(result){
                log.info("上传成功");
                if(!export){
                    pipedOutputStream.write(1);
                }

            }else {
                log.error("上传失败");
                pipedOutputStream.write(0);
            }
        } catch (Exception e) {
            log.error("上传失败IOException", e);
        }finally {
            /* 删除本地的临时缓存文件 */
            if(tempFile != null && tempFile.exists()){
                tempFile.delete();
            }
            if(ftpClient != null && ftpClient.isConnected()){
                try {
                    ftpClient.disconnect();
                } catch (IOException ignored) {
                }
            }
        }

        /* 非断点上传 */
        /*try {
            FTPClient ftpClient = FTPUtil.getFTPClient(ftpConnectAttr);
            if(ftpClient == null){
                log.error("FTPClient获取失败，上传失败");
                pipedOutputStream.write(0);
                return;
            }

            //设置上传目录,并自动创建目录
            if(!ftpClient.changeWorkingDirectory(directory)){
                log.info("上传目录不存在，开始自动创建目录");
                String[] dirs = directory.split("/");
                List<String> dirList = new ArrayList<>();
                for (String dir : dirs) {
                    if(!dir.equals("")){
                        if(dirList.size() == 0){
                            dirList.add("/" + dir);
                        }else{
                            dirList.add(dirList.get(dirList.size()-1) + "/" + dir);
                        }
                    }
                }
                for (String s : dirList) {
                    if(!ftpClient.changeWorkingDirectory(s)){
                        if(!ftpClient.makeDirectory(s)){
                            log.error("目录创建失败，上传失败");
                            pipedOutputStream.write(0);
                            return;
                        }
                    }
                }
                if(ftpClient.changeWorkingDirectory(directory)){
                    log.info("目录创建成功，目标路径更改成功：" + directory);
                }
            }
            log.info("目标路径更改成功：" + directory);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            //设置文件类型（二进制）
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            log.info("上传文件到ftp服务器，位置：" + directory + fileName);
            boolean result = ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            ftpClient.disconnect();
            if(result){
                log.info("上传成功");
                pipedOutputStream.write(1);
            }
            else{
                log.info("上传失败");
                pipedOutputStream.write(0);
            }
        } catch (IOException e) {
            log.error("上传失败IOException");
            e.printStackTrace();
        }*/
    }


    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile  本地文件 File句柄，绝对路径
     * @param ftpClient  FTPClient 引用
     * @param remoteSize 远程已上传的文件大小
     * @return
     * @throws IOException
     */
    private boolean uploadFile(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException {
        //显示进度的上传
        long step = localFile.length() / 100;
        long process = 0;
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
        //断点续传
        if (remoteSize > 0) {
            ftpClient.setRestartOffset(remoteSize);
            process = remoteSize / step;
            raf.seek(remoteSize);
            localreadbytes = remoteSize;
        }
        /* 每一次上传的数据量 */
        byte[] bytes = new byte[1024*10];
        int c;
        while ((c = raf.read(bytes)) != -1) {
            out.write(bytes, 0, c);
            localreadbytes += c;
            if (localreadbytes / step != process) {
                process = localreadbytes / step;
                log.info("上传进度:" + process);
            }
        }
        out.flush();
        raf.close();
        out.close();
        return ftpClient.completePendingCommand();
    }

    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param pipedOutputStream 输出的连接管道流
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile  本地文件 File句柄，绝对路径
     * @param ftpClient  FTPClient 引用
     * @param remoteSize 远程已上传的文件大小
     * @return
     * @throws IOException
     */
    private boolean uploadFile(PipedOutputStream pipedOutputStream,String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException {
        //显示进度的上传
        long step = localFile.length() / 100;
        long process = 0;
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
        //断点续传
        if (remoteSize > 0) {
            ftpClient.setRestartOffset(remoteSize);
            process = remoteSize / step;
            raf.seek(remoteSize);
            localreadbytes = remoteSize;
        }
        /* 每一次上传的数据量 */
        byte[] bytes = new byte[1024*10];
        int c;
        while ((c = raf.read(bytes)) != -1) {
            out.write(bytes, 0, c);
            localreadbytes += c;
            if (localreadbytes / step != process) {
                process = localreadbytes / step;
                pipedOutputStream.write(Integer.valueOf(process+""));
                pipedOutputStream.flush();
                log.info("上传进度:" + process);
            }
        }
        out.flush();
        out.close();
        raf.close();
        return ftpClient.completePendingCommand();
    }

}
