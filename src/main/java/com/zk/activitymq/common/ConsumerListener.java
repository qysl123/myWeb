package com.zk.activitymq.common;

/**
 * Created by Administrator on 2016/5/24.
 */
public class ConsumerListener {

    public void handleMessage(String message) {
        System.out.println("ConsumerListener通过handleMessage接收到一个纯文本消息，消息内容是：" + message);
    }

}
