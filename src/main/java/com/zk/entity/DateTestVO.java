package com.zk.entity;

import com.zk.enums.ExtraItemEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ken on 2016/7/18.
 */
public class DateTestVO implements Serializable{

    private String id;
    private Long num;
    private Date time;
    private ExtraItemEnum itemEnum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public ExtraItemEnum getItemEnum() {
        return itemEnum;
    }

    public void setItemEnum(ExtraItemEnum itemEnum) {
        this.itemEnum = itemEnum;
    }

    @Override
    public String toString() {
        return "DateTestVO{" +
                "id='" + id + '\'' +
                ", num=" + num +
                ", time=" + time +
                ", itemEnum=" + itemEnum +
                '}';
    }
}
