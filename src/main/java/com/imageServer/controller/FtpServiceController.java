package com.imageServer.controller;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/29 0029.
 */

import com.imageServer.service.FtpImageCacheService;
import com.imageServer.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * FTP控制器层的服务类
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/4/29 0029.
 */
@Controller("imageFtpController")
public class FtpServiceController {

    private Logger log = LoggerFactory.getLogger(FtpServiceController.class);

    @Autowired
    private FtpImageCacheService ftpImageCacheService;
    private float signAlpha = (float) 1;
    private float alpha = (float) 1;

    /**
     * 获取FTP文件服务器上的图像
     * 以response方式写出
     * 以下情况会返回 图片获取失败 的提示图像：
     * 1、远程的图片文件获取失败
     * 2、没有传入图片的path参数
     * 以下情况或返回 图片参数错误 的提示图像：
     * 1、scale参数非法
     * 2、width和height参数非法
     *
     * @param request  参数说明：
     *                 path ：String 待获取的图像文件在FTP服务器上的全路径
     *                 alpha : 图片的透明度（0.0到1.0之间的float值,默认1.0）
     *                 scale : int 对图像进行缩放处理，小于100为缩小，大于100为放大
     *                 width : int 指定图像的输出宽度
     *                 height ： int 指定图像的输出高度
     *                 sign : false：不添加版权水印 默认true (小于100 * 50 的不加水印)
     *                 signAlpha : 水印的透明度（0.0到1.0之间的float值,默认1.0）
     *                 注意：
     *                 1、width与height成对出现才会生效
     *                 2、如果指定了width和height，那么scale参数将会失效
     * @param response 写出图像
     */
    @RequestMapping(value = "image", method = RequestMethod.GET)
    public void getImage(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getParameter("path"),
                update = request.getParameter("update");

        response.setContentType("image/jpeg");
        response.setHeader("ragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            log.info("判断是否已经有请求文件的缓存图片");
            String cacheFileName = "";
            if(path != null && !path.trim().equals("") && path.lastIndexOf(".") != -1){
                cacheFileName = ftpImageCacheService.getCacheFileHashName(request);
                if(File.separator.equals("\\")){
                    cacheFileName = cacheFileName.replace("/",File.separator);
                }
                //存放缓存文件的绝对路径
                String cacheFilePath = ftpImageCacheService.getCacheFileAbsPath(request);
                File cacheFile = new File(cacheFilePath);
                log.info("计算缓存文件绝对路径：" + cacheFilePath);

                if(!"true".equals(update)){
                    if(cacheFile.exists() && cacheFile.length() > 0){
                        log.info("已有缓存文件，直接返回缓存文件流:" + cacheFile.getAbsolutePath());
                        ImageUtil.getInstance().writeImage(cacheFileName.substring(cacheFileName.lastIndexOf(".")),ImageIO.read(cacheFile),response.getOutputStream());
                        return;
                    }
                }

                if("true".equals(update)){
                    log.info("进行图片更新操作");
                    if(ftpImageCacheService.imageToCache(request)){
                        log.info("更新成功,写出图片流");
                        ImageUtil.getInstance().writeImage(cacheFileName.substring(cacheFileName.lastIndexOf(".")),ImageIO.read(cacheFile),response.getOutputStream());
                    }else{
                        log.info("图片更新失败,写出默认图片");
                        ImageUtil.getInstance().writeImage(".jpg",ImageUtil.getInstance().getNoImage(),response.getOutputStream());
                    }
                }else {
                    if(ftpImageCacheService.imageToCache(request)){
                        log.info("处理成功,写出图片流");
                        ImageUtil.getInstance().writeImage(cacheFileName.substring(cacheFileName.lastIndexOf(".")),ImageIO.read(cacheFile),response.getOutputStream());
                    }else{
                        log.info("图片处理失败,写出默认图片");
                        ImageUtil.getInstance().writeImage(".jpg",ImageUtil.getInstance().getNoImage(),response.getOutputStream());
                    }
                }

            }
        } catch (IOException e) {
            log.warn("返回缓存文件流时发生异常",e);
        }

    }

}
