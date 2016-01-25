package com.zk.base;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;

/**
 * Created by x on 2016/1/25.
 */
public class Consumer implements Runnable{

    private ArrayBlockingQueue<String> queue;
    private String str;

    public Consumer(ArrayBlockingQueue<String> queue, String str) {
        this.queue = queue;
        this.str = str;
    }

    public String consumer(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        while (true){
            System.out.print(str+"开始进行获取:");
            System.out.println(consumer());
        }

    }
}
