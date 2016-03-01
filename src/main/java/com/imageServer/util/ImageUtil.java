package com.imageServer.util;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */

import com.imageServer.vo.ImageConvert;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;

/**
 * 图片处理工具类
 * 单例模式
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/7/9 0009.
 */
public class ImageUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    private static ImageUtil imageUtil = null;

    private ImageUtil() {
    }

    /**
     * 获取网站的根目录
     * @param request
     * @return
     */
    public static String getWebPath(HttpServletRequest request){
        String webPath = request.getSession().getServletContext().getRealPath("/");
        if(webPath.lastIndexOf(File.separator) != webPath.length()-1){
            webPath = webPath + File.separator;
        }
        return webPath;
    }

    public static ImageUtil getInstance() {
        if (imageUtil == null) {
            imageUtil = new ImageUtil();
        }
        return imageUtil;
    }

    /**
     * 图片转换操作
     * 注意：
     * 1、width与height成对出现才会生效
     * 2、如果指定了width和height，那么scale参数将会失效
     * @param image
     * 待转换的图像
     * @param height_str
     * 指定图像的输出高度
     * @param width_str
     * 指定图像的输出宽度
     * @param sign
     * false：不添加版权水印 默认true (小于100 * 50 的不加水印)
     * @param recomment
     * true：添加重点推荐水印，默认false
     * @param signAlpha
     * 水印的透明度（0.0到1.0之间的float值,默认1.0）
     * @param scale_str
     * 对图像进行缩放处理，小于100为缩小，大于100为放大
     * @param alpha
     * 图片的透明度（0.0到1.0之间的float值,默认1.0）
     * @return
     */
    public static ImageConvert imageConvert(BufferedImage image,String height_str,String width_str
            ,boolean sign,boolean recomment,float signAlpha,String scale_str,float alpha){
        ImageConvert convert = new ImageConvert();
        if(height_str != null && width_str != null){//转换图像到指定高宽
            int width = 0,height = 0;
            try {
                width = Integer.parseInt(width_str);
                height = Integer.parseInt(height_str);
                image = ImageUtil.getInstance().turnImageToWH(width,height,image,sign,recomment,signAlpha);
                convert.setSucess(true);
            } catch (NumberFormatException e) {//输出提示错误图像
                log.error("图像输出错误", e);
                image = ImageUtil.getInstance().getNoImageByConvert(height_str,width_str,sign,recomment,signAlpha,scale_str,alpha);
                convert.setSucess(false);
            }
        }else if (scale_str != null) {//转换图像的等比缩放
            int scale = 100;
            try {
                scale = Integer.parseInt(scale_str);
                image = ImageUtil.getInstance().scale(scale,image,sign,recomment,signAlpha);
                convert.setSucess(true);
            } catch (NumberFormatException e) {//输出提示错误图像
                log.error("图像输出错误", e);
                image = ImageUtil.getInstance().getNoImageByConvert(height_str, width_str, sign, recomment, signAlpha, scale_str, alpha);
                convert.setSucess(false);
            }
        }else{
            if(sign){
                image = ImageUtil.getInstance().addSign(image,signAlpha);
            }
            convert.setSucess(true);
        }

        //调整图像透明度
        ImageUtil.getInstance().getImageAlpha(image, alpha);

        convert.setBufferedImage(image);

        return convert;
    }

    /**
     * 调整图像透明度
     *
     * @param image
     * @param alpha
     */
    public void getImageAlpha(BufferedImage image, float alpha) {
        if(alpha != 1){
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            bufferedImage = g2.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
            g2.dispose();
            g2 = bufferedImage.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(
                    image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_SMOOTH),
                    0, 0, null);

            g2.dispose();
        }
    }

    /**
     * response写出图片
     *
     * 若发生异常则写出默认图片
     *
     * @param fileType .jpg .gif .png
     * @param image
     * @param servletOutputStream
     * @throws IOException
     */
    public void writeImage(String fileType, BufferedImage image, ServletOutputStream servletOutputStream) throws IOException {
        try {
            imageWrite(fileType,image,servletOutputStream);
        } catch (Exception e) {
            ImageIO.write(getNoImage(), "JPEP", servletOutputStream);
        } finally {
            servletOutputStream.close();
        }
    }

    /**
     * response写出图片
     *
     * 若发生异常则写出默认图片
     *
     * @param fileType .jpg .gif .png
     * @param imagePath
     * @param servletOutputStream
     * @throws IOException
     */
    public void writeImage(String fileType, String imagePath, ServletOutputStream servletOutputStream) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            imageWrite(fileType,image,servletOutputStream);
        } catch (Exception e) {
            ImageIO.write(getNoImage(), "JPEP", servletOutputStream);
        } finally {
            servletOutputStream.close();
        }
    }

    /**
     * response写出图片，同时保存请求图片的缓存文件
     *
     * 若发生异常则写出默认图片
     *
     * @param fileType .jpg .gif .png
     * @param image
     * @param servletOutputStream
     * @param cacheFile
     * @throws IOException
     */
    public void writeImage(String fileType, BufferedImage image, ServletOutputStream servletOutputStream, File cacheFile) throws IOException {
        try {
            imageWrite(fileType,image,servletOutputStream);
        } catch (Exception e) {
            ImageIO.write(getNoImage(), "JPEP", servletOutputStream);
            //若缓存文件保存失败，则删除缓存文件
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
        } finally {
            servletOutputStream.close();
        }
    }

    /**
     * 图片写出流方法
     * @param fileType .jpg .gif .png
     * @param image
     * @param servletOutputStream
     * @throws IOException
     */
    private void imageWrite(String fileType,BufferedImage image,ServletOutputStream servletOutputStream) throws IOException {
        switch (fileType) {
            case ".jpg":{
                Thumbnails.Builder builder = Thumbnails.of(image).scale(1f)
//                            .outputQuality(0.38f)
                        .outputFormat("jpg");
                builder.toOutputStream(servletOutputStream);

//                    ImageIO.write(image, "PNG", servletOutputStream);
                break;
            }
            case ".gif":{
                ImageIO.write(image, "GIF", servletOutputStream);
                break;
            }
            case ".png":{
                Thumbnails.Builder builder = Thumbnails.of(image).scale(1f)
//                            .outputQuality(0.38f)
                        .outputFormat("jpg");
                builder.toOutputStream(servletOutputStream);

//                    ImageIO.write(image, "PNG", servletOutputStream);
                break;
            }
        }
    }

    /**
     * 保存请求图片的缓存文件
     *
     * @param fileType .jpg .gif .png
     * @param image
     * @param cacheFile
     * @throws IOException
     */
    public void writeImage(String fileType, BufferedImage image, File cacheFile) throws IOException {
        try {
            switch (fileType) {
                case ".jpg":{
                    Thumbnails.Builder builder = Thumbnails.of(image).scale(1f)
//                            .outputQuality(0.38f)
                            .outputFormat("jpg");
                    builder.toFile(cacheFile);

//                    ImageIO.write(image, "PNG", cacheFile);
                    break;
                }
                case ".gif":{
                    ImageIO.write(image, "GIF", cacheFile);
                    break;
                }
                case ".png":{
                    Thumbnails.Builder builder = Thumbnails.of(image).scale(1f)
//                            .outputQuality(0.38f)
                            .outputFormat("jpg");
                    builder.toFile(cacheFile);

//                    ImageIO.write(image, "PNG", cacheFile);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //若缓存文件保存失败，则删除缓存文件
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
        }
    }


    /**
     * 返回指定高宽的图片
     * 按照中间的位置自动裁剪图片
     * @param width
     * 指定的宽
     * @param height
     * 指定的高
     * @param image
     * 需要进行处理的源图片
     * @param sign
     * 是否添加版权水印
     * @param recomment
     * 是否添加推荐水印
     * @param signAlpha
     * 水印透明度
     * @return
     */
    public BufferedImage turnImageToWH(int width, int height, BufferedImage image, boolean sign, boolean recomment, float signAlpha) {

        /**
         * 转换图像（高宽至少有目标高宽的最大值）
         */
        double z;
        if (image.getWidth() == image.getHeight()) {
            int s = width > height ? width : height;
            z = CustomerMath.div(s, image.getHeight(), 2);
        } else if (width > height) {
            if (image.getHeight() > image.getWidth()) {
                z = CustomerMath.div(width, image.getWidth(), 2);
            } else {
                z = CustomerMath.div(height, image.getHeight(), 2);
            }
        } else if (width < height) {
            if (image.getHeight() > image.getWidth()) {
                z = CustomerMath.div(width, image.getWidth(), 2);
            } else {
                z = CustomerMath.div(height, image.getHeight(), 2);
            }
        } else {
            int cut = image.getWidth() <= image.getHeight() ? image.getWidth() : image.getHeight();
            z = CustomerMath.div(width, cut, 2);
        }

        double tw = CustomerMath.mul(z,image.getWidth());
        double th = CustomerMath.mul(z,image.getHeight());
        while (tw < width || th < height){
            z = z + 0.01;
            tw = CustomerMath.mul(z,image.getWidth());
            th = CustomerMath.mul(z,image.getHeight());
        }

        int z1 = (int) CustomerMath.mul(z, 100);

        // 将图像等比缩放至至合适指定高宽的大小
        BufferedImage image1 = scale(z1, image, false, false, signAlpha);

        // 计算裁剪的中间位置开始坐标
        int x = 0,y = 0;
        if (image1.getWidth() > width) {
            x = (int) (CustomerMath.div(image1.getWidth(), 2) - CustomerMath.div(width, 2));
        }
        if (image1.getHeight() > height) {
            y = (int) (CustomerMath.div(image1.getHeight(), 2) - CustomerMath.div(height, 2));
        }

        // 从缩放的图像中间位置进行裁剪，成为指定的高宽的图像
        BufferedImage result = cut(image1,x,y,width,height);

        // 是否添加版权水印
        if(sign)
            result = addSign(result,signAlpha);

        // 是否添加推荐水印
        if(recomment)
            result = addRecommentSign(result);

        return result;
    }

    /**
     * 图像切割（指定切片的宽度和高度）
     * @param bi 原图像
     * @param x 裁剪原图像起点坐标X
     * @param y 裁剪原图像起点坐标Y
     * @param width 目标切片宽度
     * @param height 目标切片高度
     * @return
     */
    public static BufferedImage cut(BufferedImage bi,int x, int y, int width, int height) {

        BufferedImage tag = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = tag.createGraphics();
        tag = g2.getDeviceConfiguration().createCompatibleImage(width, height,
                Transparency.TRANSLUCENT);
        g2.dispose();
        g2 = tag.createGraphics();

        int srcWidth = bi.getHeight(); // 源图宽度
        int srcHeight = bi.getWidth(); // 源图高度
        if (srcWidth > 0 && srcHeight > 0) {
            ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
            Image img = Toolkit.getDefaultToolkit().createImage(
                    new FilteredImageSource(bi.getSource(),cropFilter));
            g2.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
            g2.dispose();
        }
        return tag;
    }

    /**
     * 等比缩放
     *
     * @param scale
     * @param scaleImage
     * @param sign
     * @return
     */
    public BufferedImage scale(int scale, BufferedImage scaleImage, boolean sign, boolean recomment, float signAlpha) {
        if(scale != 100){
            int width = (int) (scaleImage.getWidth(null) * scale / 100.0);
            int height = (int) (scaleImage.getHeight(null) * scale / 100.0);

            BufferedImage bufferedImage = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            bufferedImage = g2.getDeviceConfiguration().createCompatibleImage(width, height,
                    Transparency.TRANSLUCENT);
            g2.dispose();
            g2 = bufferedImage.createGraphics();

            g2.drawImage(
                    scaleImage.getScaledInstance(width, height, Image.SCALE_SMOOTH),
                    0, 0, null);

            try {
                bufferedImage = Thumbnails.of(bufferedImage).scale(1f)
                        .outputQuality(0.38f)
                        .asBufferedImage();
            } catch (IOException e) {
                log.error("更改图片质量失败",e);
            }

            if (sign) {
                bufferedImage = addSign(bufferedImage, signAlpha);
            }
            if (recomment) {
                bufferedImage = addRecommentSign(bufferedImage);
            }
            return bufferedImage;
        }else {
            try {
                BufferedImage bufferedImage = scaleImage;
                if(!sign && !recomment){
                    bufferedImage = Thumbnails.of(scaleImage).scale(1f)
                            .outputQuality(0.40f)
                            .asBufferedImage();
                }
                if (sign) {
                    bufferedImage = addSign(bufferedImage, signAlpha);
                }
                if (recomment) {
                    bufferedImage = addRecommentSign(bufferedImage);
                }
                return bufferedImage;
            } catch (IOException e) {
                log.error("降低图片质量失败",e);
                return scaleImage;
            }
        }
    }

    /**
     * 获取图片未找到的提示图片
     *
     * @return
     */
    public BufferedImage getNoImage() {
        String markImgPath = this.getClass().getClassLoader().getResource("noimage.jpg").getPath();
        File file = new File(markImgPath);
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            image = new BufferedImage(100, 35,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setFont(new Font("宋体", Font.BOLD, 15));
            g.setColor(Color.CYAN);
            g.drawString("图片获取失败", 0, 25);
            g.dispose();
        }
        return image;
    }

    /**
     * 获取图片未找到的提示图片
     * 并进行图片转换
     * @param height_str
     * @param width_str
     * @param sign
     * @param recomment
     * @param signAlpha
     * @param scale_str
     * @param alpha
     * @return
     */
    public BufferedImage getNoImageByConvert(String height_str,String width_str
            ,boolean sign,boolean recomment,float signAlpha,String scale_str,float alpha){

        String path = this.getClass().getClassLoader().getResource("noimage.jpg").getPath();

        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
        } catch (Exception ignored) {
        }

        String cacheFileName = "";
        if(request != null){
            cacheFileName = ImageCatchUtil.getImageCachePathByFtpPath(path,sign,recomment,height_str,width_str,scale_str,signAlpha,alpha);
            if(File.separator.equals("\\")){
                cacheFileName = cacheFileName.replace("/",File.separator);
            }
            File cacheFile = new File(request.getSession().getServletContext().getRealPath("/") + "/" + cacheFileName);
            if(cacheFile.exists() && cacheFile.length() > 0){
                try {
                    log.info("返回已缓存的错误图片" + cacheFile.getPath());
                    return ImageIO.read(cacheFile);
                } catch (IOException ignored) {
                }
            }
        }

        BufferedImage image = getNoImage();
        if(height_str != null && width_str != null){//转换图像到指定高宽
            int width = 0,height = 0;
            try {
                width = Integer.parseInt(width_str);
                height = Integer.parseInt(height_str);
                image = ImageUtil.getInstance().turnImageToWH(width,height,image,false,false,signAlpha);
            } catch (NumberFormatException e) {//输出提示错误图像
                log.error("图像输出错误", e);
                image = ImageUtil.getInstance().getNoImage();
            }
        }else if (scale_str != null) {//转换图像的等比缩放
            int scale = 100;
            try {
                scale = Integer.parseInt(scale_str);
                image = ImageUtil.getInstance().scale(scale,image,false,false,signAlpha);
            } catch (NumberFormatException e) {//输出提示错误图像
                log.error("图像输出错误", e);
                image = ImageUtil.getInstance().getNoImage();
            }
        }else{
            if(sign){
                image = ImageUtil.getInstance().addSign(image,signAlpha);
            }
        }

        //调整图像透明度
        ImageUtil.getInstance().getImageAlpha(image,alpha);

        try {
            log.info("将指定格式的错误图片的缓存文件保存");
            String webPath = ImageUtil.getWebPath(request);
            String cacheDir = (webPath + path.substring(1,path.lastIndexOf("/")+1))
                    .replace("/",File.separator);
            String fileType = path.substring(path.lastIndexOf("."));
            log.info("计算获取文件后缀名：" + fileType);

            File cacheFile = new File(webPath + cacheFileName);
            File dir = new File(webPath + File.separator + "error");
            if(!dir.exists()){
                dir.mkdirs();
            }
            writeImage(fileType,image,cacheFile);
        } catch (Exception e) {
            log.error("错误图片的缓存文件保存失败",e);
        }

        return image;
    }

    /**
     * 添加版权水印
     * 宽度小于250，高度小于250的不添加水印
     * @param image
     * @return
     */
    public BufferedImage addSign(BufferedImage image, float signAlpha) {
        if(image.getHeight() < 250 || image.getWidth() < 250){
            return image;
        }
        Graphics g = image.getGraphics();
        g.setFont(new Font("宋体", Font.BOLD, 15));
        g.setColor(Color.GRAY);
        Graphics2D g2 = image.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, signAlpha));

        try {
            String markImgPath = this.getClass().getClassLoader().getResource("watermark.png").getPath();
            File file = new File(markImgPath);
            BufferedImage markImg = ImageIO.read(file);
            g2.drawImage(markImg, image.getWidth() - markImg.getWidth() - 9, image.getHeight() - markImg.getHeight() - 9, null);
        } catch (IOException e) {
            g2.drawString("盛世文化", image.getWidth() - 80, image.getHeight() - 25);
            g2.drawString("版权所有", image.getWidth() - 80, image.getHeight() - 10);
        }
        g.dispose();
        return image;
    }

    /**
     * 添加推荐水印
     *
     * @param image
     * @return
     */
    public BufferedImage addRecommentSign(BufferedImage image) {
        int offset = 3;//设置偏移量
        String markImgPath = this.getClass().getClassLoader().getResource("tuijianBig.png").getPath();
        if(image.getWidth() <= 80 ){
            markImgPath = this.getClass().getClassLoader().getResource("tuijianSmall.png").getPath();
            offset = 2;
        }

        // 创建透明背景图
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        bufferedImage = g2.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(),
                Transparency.TRANSLUCENT);
        g2.dispose();
        g2 = bufferedImage.createGraphics();

        // 在背景图中画上原图像
        g2.drawImage(
                image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_SMOOTH),
                offset, offset, null);

        // 画上推荐水印
        try {
            File file = new File(markImgPath);
            BufferedImage markImg = ImageIO.read(file);
            g2.drawImage(markImg, 0, 0, null);
        } catch (IOException ignored) {

        }
        g2.dispose();
        return bufferedImage;
    }

}
