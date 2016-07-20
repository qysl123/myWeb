package com.zk.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 保养、美容、精品加装 项目枚举类
 * Created by Ken on 2016/7/18.
 */
public enum ExtraItemEnum implements BaseEnum {
    MAINTAIN(0, "保养"), COSMETOLOGY(1, "美容"), INSTALLATION(2, "精品加装");

    private int id;
    private String text;
    private static List<ExtraItemEnum> list;

    static {
        list = new ArrayList<>(3);
        list.add(MAINTAIN);
        list.add(COSMETOLOGY);
        list.add(INSTALLATION);
    }

    ExtraItemEnum(int id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static List<ExtraItemEnum> getAll() {
        return list;
    }

    public static ExtraItemEnum findById(Integer id) {
        for (ExtraItemEnum item : list) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public BaseEnum getEnum(int id) {
        return findById(id);
    }
}
