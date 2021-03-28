package org.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcRequestProcessor {

    private static volatile ThreadPoolExecutor threadPoolExecutor;

    public static void submitRequest(Runnable runnable) {
        System.out.println("提交任务");
        if (threadPoolExecutor == null) {
            synchronized (RpcRequestProcessor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(
                            10, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
                }
            }
        }
        threadPoolExecutor.submit(runnable);
    }

}
