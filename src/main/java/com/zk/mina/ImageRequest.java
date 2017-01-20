package com.zk.mina;

import java.awt.image.BufferedImage;

/**
 * Created by Ken on 2016/11/4.
 */
public class ImageRequest {
    private int length;
    private byte[] imageByte;

    public ImageRequest() {
    }

    public ImageRequest(int length, byte[] image) {
        this.length = length;
        this.imageByte = image;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getImageByte() {
        return imageByte;
    }

    public void setImageByte(byte[] imageByte) {
        this.imageByte = imageByte;
    }
}
