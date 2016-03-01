package com.imageServer.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/24 0024.
 */

import com.imageServer.config.FTPConnectAttr;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * FTP工具类
 * 单例
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/24 0024.
 */
public class FTPUtil {

    private static final Logger log = LoggerFactory.getLogger(FTPUtil.class);

    private FTPUtil(){

    }

    /**
     * 获取FTP连接对象
     * @param ftpConnectAttr
     * FTP的连接配置
     * @return
     * 获取异常失败则返回null
     */
    public static FTPClient getFTPClient(FTPConnectAttr ftpConnectAttr){
        if(ftpConnectAttr == null){
            log.error("FTP的连接配置不能为Null");
            return null;
        }
        if(ObjectUtil.isNull(ftpConnectAttr.address) || ObjectUtil.isNull(ftpConnectAttr.userName)){
            log.error("连接地址或连接的用户名不能为空");
            return null;
        }
        FTPClient ftpClient = new FTPClient();
        try {
            String url = ftpConnectAttr.address;
            log.info("设置ftp地址：" + url);
            if(url.split(":").length == 2){
                String[] us = url.split(":");
                int port = Integer.valueOf(us[1]);
                ftpClient.connect(us[0],port);
            }else {
                ftpClient.connect(url);
            }
            ftpClient.setDataTimeout(120000);       //设置传输超时时间为120秒
            ftpClient.setConnectTimeout(120000);       //连接超时为120秒
            ftpClient.setControlEncoding("GBK");
            log.info("设置登陆账号：" + ftpConnectAttr.userName + ":" + ftpConnectAttr.password);
            ftpClient.login(ftpConnectAttr.userName, ftpConnectAttr.password);
            int reply = ftpClient.getReplyCode();
            if(String.valueOf(reply).indexOf("2") != 0){
                log.error("ftp连接失败，返回值：" + reply);
                if(ftpClient.isConnected()){
                    ftpClient.disconnect();
                    return null;
                }
            }
            log.info("ftp连接成功，返回值：" + reply);
        } catch (IOException e) {
            log.error("获取FTPClient发生错误,返回null",e);
            if(ftpClient.isConnected()){
                try {
                    ftpClient.disconnect();
                } catch (IOException ignored) {
                }
            }
            return null;
        }
        return ftpClient;
    }

}
