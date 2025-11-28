package cn.smartjavaai.common.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dwj
 * @date 2025/11/26
 */
public class GlobalExecutor {

    private static volatile ExecutorService executor;

    public static ExecutorService getExecutor() {
        if (executor == null) {
            synchronized (GlobalExecutor.class) {
                if (executor == null) {
                    int cores = Runtime.getRuntime().availableProcessors();
                    executor = new ThreadPoolExecutor(
                            cores,
                            cores * 2,
                            60L, TimeUnit.SECONDS,
                            new SynchronousQueue<>(),
                            runnable -> {
                                Thread t = new Thread(runnable);
                                t.setDaemon(true); // 守护线程
                                return t;
                            },
                            new ThreadPoolExecutor.DiscardOldestPolicy()
                    );
                }
            }
        }
        return executor;
    }

    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
