package com.imageServer.config;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 16/2/27.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * FTP连接的帐号属性
 * Copyright 2015-2016 the original ql
 * Created by QianLong on 16/2/27.
 */
@Component
public class FTPConnectAttr {
    @Value("${ftp.address}")
    public String address;
    @Value("${ftp.userName}")
    public String userName;
    @Value("${ftp.password}")
    public String password;
}
