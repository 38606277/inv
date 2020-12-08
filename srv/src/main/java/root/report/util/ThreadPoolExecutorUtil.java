package root.report.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther:
 * @Date: 2018/11/5 15:23
 * @Description: 线程池Util类，初始化线程池
 */
public class ThreadPoolExecutorUtil {

    // 线程池 单例化:全局唯一配置
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(10,20,
            60L,TimeUnit.SECONDS,
            new SynchronousQueue<>(),Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    public ThreadPoolExecutorUtil(){};

    public static ThreadPoolExecutor getInstance(){
        return executor;
    }

}
