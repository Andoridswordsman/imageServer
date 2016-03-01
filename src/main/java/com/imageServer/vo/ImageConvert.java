package com.imageServer.vo;/**
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/14 0014.
 */

import java.awt.image.BufferedImage;

/**
 * 图像转换结果实体
 * Copyright 2014-2015 the original ql
 * Created by QianLong on 2014/8/14 0014.
 */
public class ImageConvert {

    private BufferedImage bufferedImage;
    private boolean sucess;

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public boolean isSucess() {
        return sucess;
    }

    public void setSucess(boolean sucess) {
        this.sucess = sucess;
    }
}
