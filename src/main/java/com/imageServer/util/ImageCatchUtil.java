package com.imageServer.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */

/**
 * 图片缓存机制工具类
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */
public class ImageCatchUtil {

    /**
     * 组建文件hash路径名
     * 如：images/paintings/1/xxxx.png
     * @param path
     * @param sign
     * @param recomment
     * @param height_str
     * @param width_str
     * @param scale_str
     * @param signAlpha
     * @param alpha
     * @return
     */
    public static String getImageCachePathByFtpPath(String path,boolean sign,boolean recomment,String height_str,
                                                    String width_str,String scale_str,float signAlpha,float alpha){
        String cacheFileName = "";
        if(path != null && !path.trim().equals("") && path.lastIndexOf(".") != -1 && path.lastIndexOf("/") < path.length()-1) {

            //控制缓存文件的后缀名
            String cacheFileType = ".jpg";
            if (path.substring(path.lastIndexOf(".")).equals(".gif")) {
                cacheFileType = ".gif";
            }

            //获取ftp服务器path上的文件名
            String fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));

            //文件名的hash算法
            if (sign) {
                fileName = "sign" + fileName;
            }
            if (recomment) {
                fileName = "reco" + fileName;
            }
            if (height_str != null && width_str != null) {
                fileName = height_str + width_str + fileName;
            } else if (scale_str != null) {
                fileName = "scale" + scale_str + fileName;
            }
            if (signAlpha != 1) {
                fileName = "signAlpha" + (signAlpha * 10) + fileName;
            }
            if (alpha != 1) {
                fileName = "alpha" + (alpha * 10) + fileName;
            }

            //组建最后的文件相对路径 如 ： images/paintings/1/xxxx.png
            if(path.indexOf("/") == 0 || path.indexOf("\\") == 0){
                cacheFileName = path.substring(1, path.lastIndexOf("/") + 1) + fileName + cacheFileType;
            }else {
                cacheFileName = path.substring(0, path.lastIndexOf("/") + 1) + fileName + cacheFileType;
            }

        }
        return cacheFileName;
    }


//    /**
//     * 保存缓存文件的信息
//     * dir/info.xml
//     * dir:缓存文件的目录
//     * @param file
//     * 缓存的文件
//     * @param requestUrl
//     * 生成该缓存文件时，请求的url
//     */
//    public static void saveCatchImageInfo(File file,String requestUrl){
//        String dir = file.getParent();
//        File infoFile = new File(dir + "info.xml");
//
//
//
//    }

}
