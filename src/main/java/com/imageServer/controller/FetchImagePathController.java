package com.imageServer.controller;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */

import com.imageServer.service.FtpImageCacheService;
import com.imageServer.util.ImageCatchUtil;
import com.imageServer.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 获取图片的地址控制器
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */
@Controller
public class FetchImagePathController {

    Logger log = LoggerFactory.getLogger(FetchImagePathController.class);

    @Autowired
    private FtpImageCacheService ftpImageCacheService;
    @Value("${sys.webUrl}")
    private String webUrl;

    /**
     * 获取图片静态资源的hashUrl网址
     * @param request
     * @param response
     */
    @RequestMapping(value = "getImagePath",method = RequestMethod.GET)
    @ResponseBody
    public void getImagepath(HttpServletRequest request,HttpServletResponse response){
        log.info(request.getRemoteAddr() + "获取图片的静态资源路径");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            return;
        }

        String path = request.getParameter("path"),
                scale_str = request.getParameter("scale"),
                width_str = request.getParameter("width"),
                height_str = request.getParameter("height"),
                sign_str = request.getParameter("sign"),
                signAlpha_str = request.getParameter("signAlpha"),
                alpha_str = request.getParameter("alpha"),
                recommentSign_str = request.getParameter("recommentSign"),
                update = request.getParameter("update");

        float signAlpha = (float) 1;
        float alpha = (float) 1;

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

        log.info("图片系统对外的网站地址：" + webUrl);
        // 错误提示图片地址
        String errorImgPath = webUrl + "/" + "image?" + request.getQueryString();

        String cacheFileName = "";
        if(path != null && !path.trim().equals("") && path.lastIndexOf(".") != -1){

            cacheFileName = ImageCatchUtil.getImageCachePathByFtpPath(path, sign, recomment, height_str, width_str, scale_str, signAlpha, alpha);
            log.info("获取图片静态资源的hash相对路径：" + cacheFileName);
            String cacheFileName1 = cacheFileName;

            //对平台的路径分隔符进行判断
            if(File.separator.equals("\\")){
                cacheFileName = cacheFileName.replace("/", "\\");
            }

            String webPath = ImageUtil.getWebPath(request);

            log.info("根据hash路径生成缓存文件绝对路径：" + webPath + cacheFileName);
            File cacheFile = new File(webPath + cacheFileName);

            String reUrl = webUrl + "/" + cacheFileName1;

            String cacheDir = (webPath + path.substring(1,path.lastIndexOf("/")+1))
                    .replace("/",File.separator);


            if(update != null && update.equals("true")){//进行缓存文件的更新操作
                log.info("进行缓存文件更新操作，待更新的缓存文件：" + cacheFileName1);
                if(ftpImageCacheService.imageToCache(request)){
                    log.info("缓存文件更新成功，返回静态资源网址：" + reUrl);
                    out.write(reUrl);
                }else {
                    File f = new File(cacheDir + cacheFileName);
                    if(f.exists()){
                        if(f.length() > 0){
                            log.info("缓存文件更新失败，返回原来的静态资源网址：" + reUrl);
                            out.write(reUrl);
                        }else {
                            log.warn("缓存文件更新失败，返回动态图片请求链接：" + errorImgPath);
                            out.write(errorImgPath);
                        }
                    }else {
                        log.warn("缓存文件更新失败，返回动态图片请求链接：" + errorImgPath);
                        out.write(errorImgPath);
                    }
                }
            }else {
                if(cacheFile.exists()){
                    log.info("缓存文件已生成，返回已存在的静态资源网址：" + reUrl);
                    out.write(reUrl);
                }else {
                    log.info("缓存文件不存在，正在生成缓存文件");
                    if(ftpImageCacheService.imageToCache(request)){
                        log.info("缓存文件生成成功，返回静态资源网址：" + reUrl);
                        out.write(reUrl);
                    }else {
                        log.warn("缓存文件生成失败，返回动态图片请求链接：" + errorImgPath);
                        out.write(errorImgPath);
                    }
                }
            }

        }else {
            out.write(errorImgPath);
            log.warn("传入的path无效，返回动态图片请求链接:" + errorImgPath);
        }

    }

}
