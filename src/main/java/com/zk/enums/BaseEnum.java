package com.zk.enums;

/**
 *
 * Created by Ken on 2016/7/18.
 */
public interface BaseEnum {
    int getId();
    String getText();
    BaseEnum getEnum(int id);
}
