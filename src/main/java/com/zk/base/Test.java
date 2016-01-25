package com.zk.base;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by x on 2016/1/25.
 */
public class Test {

    public static void main(String[] args){
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(6);

        pool.execute(new Consumer(queue, "消费者1"));
        pool.execute(new Producer(queue,"生产者1"));
        pool.execute(new Producer(queue,"生产者2"));
        pool.execute(new Consumer(queue, "消费者2"));
        pool.execute(new Consumer(queue, "消费者3"));
    }

}
