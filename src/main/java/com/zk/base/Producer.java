package com.zk.base;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by x on 2016/1/25.
 */
public class Producer implements Runnable{

    private ArrayBlockingQueue<String> queue;
    private String str;


    public Producer(ArrayBlockingQueue<String> queue, String str) {
        this.queue = queue;
        this.str = str;
    }

    public void producer(String s){
        try {
            queue.put(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 100; i++){
            if (i/2 == 1){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(str+"开始生产");
            producer(str+":"+i);
        }

    }
}
