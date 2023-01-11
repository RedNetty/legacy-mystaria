package me.retrorealms.practiceserver.mechanics.dungeon.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Giovanni on 2-5-2017.
 */
public class AsyncTask {

    private long delay, interval;
    private final Runnable task;
    private boolean useSharedThreadPool;

    public final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    /**
     * @param task The task you wish to be executed Async.
     */
    public AsyncTask(Runnable task) {
        this.task = task;
    }

    /**
     * @param delay The delay you wish your task to be ran after.
     * @return The altered AsyncTask.
     */
    public AsyncTask setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    /**
     * @return The altered AsyncTask.
     */
    public AsyncTask setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    /**
     * @param useSharedPool Whether or not you wish to use the shared thread pool.
     * @return The altered AsyncTask.
     */
    public AsyncTask setUseSharedPool(boolean useSharedPool) {
        this.useSharedThreadPool = useSharedPool;
        return this;
    }

    public void scheduleRepeatingTask() {
        if (this.useSharedThreadPool) {
            executorService.scheduleAtFixedRate(this.task, this.delay, this.interval, TimeUnit.SECONDS);
        } else {
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this.task, this.delay, this.interval, TimeUnit.SECONDS);
        }
    }

    public void scheduleDelayedTask() {
        if (this.useSharedThreadPool) {
            executorService.schedule(this.task, this.delay, TimeUnit.SECONDS);
        } else {
            Executors.newScheduledThreadPool(1).schedule(this.task, this.delay, TimeUnit.SECONDS);
        }
    }
}
