package com.assignment.connectnow;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultPoolExecutor extends ThreadPoolExecutor {
    private static final String TAG = DefaultPoolExecutor.class.getSimpleName();
    //    Thread args
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int INIT_THREAD_COUNT = CPU_COUNT + 1;
    private static final int MAX_THREAD_COUNT = INIT_THREAD_COUNT;
    private static final long SURPLUS_THREAD_LIFE = 30L;

    private static DefaultPoolExecutor instance;

    public static DefaultPoolExecutor getInstance()
    {
        if (null == instance)
        {
            synchronized (DefaultPoolExecutor.class)
            {
                if (null == instance)
                {
                    instance = new DefaultPoolExecutor(
                            INIT_THREAD_COUNT,
                            MAX_THREAD_COUNT,
                            SURPLUS_THREAD_LIFE,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(64),
                            new DefaultThreadFactory());
                }
            }
        }
        return instance;
    }

    private DefaultPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler()
        {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
            {
                Log.e(TAG, "Task rejected, too many task!");
            }
        });
    }

    /* thread execution complete, handle possible exceptions.
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>)
        {
            try
            {
                ((Future<?>) r).get();
            }
            catch (CancellationException ce)
            {
                t = ce;
            }
            catch (ExecutionException ee)
            {
                t = ee.getCause();
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null)
        {
            Log.w(TAG, "Running task appeared exception! Thread [" + Thread.currentThread().getName() + "], because [" + t.getMessage() + "]\n" + t.getMessage());
        }
    }
    public static class DefaultThreadFactory implements ThreadFactory
    {
        private  final String TAG = DefaultThreadFactory.class.getSimpleName();

        private  final AtomicInteger poolNumber = new AtomicInteger(1);

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;

        public DefaultThreadFactory()
        {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "ARouter task pool No." + poolNumber.getAndIncrement() + ", thread No.";
        }

        public Thread newThread(@NonNull Runnable runnable)
        {
            String threadName = namePrefix + threadNumber.getAndIncrement();
            Log.i(TAG, "Thread production, name is [" + threadName + "]");
            Thread thread = new Thread(group, runnable, threadName, 0);
            if (thread.isDaemon())
            {   //Make non-background thread
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY)
            {
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            // Catching exceptions in multi-threaded processing
            thread.setUncaughtExceptionHandler((thread1, ex) -> Log.i(TAG, "Running task appeared exception! Thread [" + thread1.getName() + "], because [" + ex.getMessage() + "]"));
            return thread;
        }
    }
}