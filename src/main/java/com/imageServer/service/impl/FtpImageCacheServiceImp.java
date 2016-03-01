package com.imageServer.service.impl;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */

import com.imageServer.service.FtpImageCacheService;
import com.imageServer.service.FtpService;
import com.imageServer.util.ImageCatchUtil;
import com.imageServer.util.ImageUtil;
import com.imageServer.util.ObjectUtil;
import com.imageServer.vo.ImageConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * FTP服务器上的图片进行缓存服务
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */
@Service("ftpImageCacheService")
public class FtpImageCacheServiceImp implements FtpImageCacheService {

    Logger log = LoggerFactory.getLogger(FtpImageCacheServiceImp.class);

    @Autowired
    private FtpService ftpService;
    private float signAlpha = (float) 1;
    private float alpha = (float) 1;

    /**
     * 根据请求条件，将原图进行转换，并生成缓存文件
     * @param request
     * @return
     */
    @Override
    public boolean imageToCache(HttpServletRequest request) {
        String path = request.getParameter("path"),
                scale_str = request.getParameter("scale"),
                width_str = request.getParameter("width"),
                height_str = request.getParameter("height"),
                sign_str = request.getParameter("sign"),
                signAlpha_str = request.getParameter("signAlpha"),
                alpha_str = request.getParameter("alpha"),
                recommentSign_str = request.getParameter("recommentSign");

        boolean sign = true;
        boolean recomment = false;
        try {
            sign = !sign_str.equals("false");
        } catch (Exception ignored) { }
        try {
            recomment = recommentSign_str.equals("true");
        } catch (Exception ignored) {
        }
        try {
            if(signAlpha_str != null && !signAlpha_str.equals("")){
                signAlpha = Float.parseFloat(signAlpha_str);
            }else {
                signAlpha = 1;
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            if(alpha_str != null && !alpha_str.equals("")){
                alpha = Float.parseFloat(alpha_str);
            }else {
                alpha = 1;
            }
        } catch (NumberFormatException ignored) {}

        String cacheFileName;
        if(path != null && !path.trim().equals("") && path.lastIndexOf(".") != -1){
            cacheFileName = getCacheFileHashName(request);
            log.info("获取图片静态资源的hash相对路径：" + cacheFileName);
            if(File.separator.equals("\\")){
                cacheFileName = cacheFileName.replace("/",File.separator);
            }

        }else {
            log.warn("ftp文件资源路径错误，返回false");
            return false;
        }

        File cacheFile = null;
        File temp = null;
        try {
            if(path.equals("")){
                return false;
            }

            String cacheDir = getCacheFileDirPath(request);
            log.info("计算待生成的缓存图片的绝对路径目录：" + cacheDir);
            String fileType;
            try {
                fileType = path.substring(path.lastIndexOf("."));
            } catch (Exception e) {
                return false;
            }
            log.info("原图片的文件类型：" + fileType);
            cacheFile = new File(getCacheFileAbsPath(request));
            log.info("计算待生成图片的绝对路径：" + cacheFile.getAbsolutePath());
            if(!cacheFile.exists()){
                log.info("待生成缓存文件的目录不存在，正在进行自动创建目录:" + cacheDir);
                File cacheFileDir = new File(cacheDir);
                cacheFileDir.mkdirs();
            }

            //若缓存文件已存在，用临时文件保存已有的缓存文件，原缓存文件进行重新保存下载的文件

            if(cacheFile.exists() && cacheFile.length() > 0){
                temp = new File(cacheFile.getPath() + ".temp");
                cacheFile.renameTo(temp);
                cacheFile.delete();
            }

            log.info("从ftp服务器下载path对应的图片资源:" + path);
            log.info("保存到文件：" + cacheFile.getAbsolutePath());
            ftpService.download(cacheFile, path);

            BufferedImage image = null;
            try {
                if(cacheFile.exists()){
                    image = ImageIO.read(cacheFile);
                }else{
                    log.warn("获取下载的图片资源失败,返回false");
                    return false;
                }
            } catch (Exception e) {
               log.warn("图片文件读取异常",e);
            }
            if (image == null) {//输出提示错误图像
                log.info("图像下载错误，返回false");
                return false;
            } else {
                log.info("图像下载成功，进行图像转换操作");
                ImageConvert convert =ImageUtil.imageConvert(image,height_str,width_str,sign,recomment,signAlpha,scale_str,alpha);
                image = convert.getBufferedImage();
                if(convert.isSucess()){
                    log.info("保存转换后的图片为缓存文件写入磁盘:" + cacheFile.getAbsolutePath());
                    ImageUtil.getInstance().writeImage(fileType,image,cacheFile);
                    if(cacheFile.exists()){
                        log.info("缓存文件生成成功");
                        //删除缓存文件
                        if (temp != null) {
                            temp.delete();
                        }
                        return true;
                    }else {
                        log.warn("缓存文件生成失败");
                        //将缓存文件还原
                        if (temp != null) {
                            temp.renameTo(cacheFile);
                        }
                        return false;
                    }
                }else {
                    log.warn("图像转换失败，返回错误提示");
                    return false;
                }
            }
        } catch (IOException e) {
            if(cacheFile.exists()){
                if(temp != null && temp.exists()){
                    temp.delete();
                }
            }else {
                if(temp != null && temp.exists()){
                    temp.renameTo(cacheFile);
                }
            }
            log.error("保存request中的图片信息到缓存文件中发生异常",e);
            return false;
        }
    }

    /**
     * 计算获取缓存文件的hash文件名
     *
     * @param request
     * @return
     */
    @Override
    public String getCacheFileHashName(HttpServletRequest request) {
        String path = request.getParameter("path"),
                scale_str = request.getParameter("scale"),
                width_str = request.getParameter("width"),
                height_str = request.getParameter("height"),
                sign_str = request.getParameter("sign"),
                signAlpha_str = request.getParameter("signAlpha"),
                alpha_str = request.getParameter("alpha"),
                recommentSign_str = request.getParameter("recommentSign");

        boolean sign = true;
        boolean recomment = false;
        try {
            sign = !sign_str.equals("false");
        } catch (Exception ignored) { }
        try {
            recomment = recommentSign_str.equals("true");
        } catch (Exception ignored) {
        }
        try {
            if(signAlpha_str != null && !signAlpha_str.equals("")){
                signAlpha = Float.parseFloat(signAlpha_str);
            }else {
                signAlpha = 1;
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            if(alpha_str != null && !alpha_str.equals("")){
                alpha = Float.parseFloat(alpha_str);
            }else {
                alpha = 1;
            }
        } catch (NumberFormatException ignored) {}

        return ImageCatchUtil.getImageCachePathByFtpPath(path, sign, recomment, height_str, width_str, scale_str, signAlpha, alpha);
    }

    /**
     * 计算获取缓存文件的绝对路径位置
     *
     * @param request
     * @return
     */
    @Override
    public String getCacheFileAbsPath(HttpServletRequest request) {

        return ImageUtil.getWebPath(request) + getCacheFileHashName(request);
    }

    /**
     * 计算获取缓存文件的目录绝对路径位置
     *
     * @param request
     * @return
     */
    @Override
    public String getCacheFileDirPath(HttpServletRequest request) {
        String path = request.getParameter("path");

        if(ObjectUtil.isNull(path)){
            log.error("请求路径不能为空");
            return "";
        }

        String webPath = ImageUtil.getWebPath(request);

        return (webPath + path.substring(1,path.lastIndexOf("/")+1))
                .replace("/",File.separator);
    }
}
