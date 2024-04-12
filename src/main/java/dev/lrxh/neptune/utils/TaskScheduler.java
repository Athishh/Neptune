package dev.lrxh.neptune.utils;

import dev.lrxh.neptune.Neptune;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class TaskScheduler {
    private final HashSet<BukkitRunnable> tasks = new HashSet<>();

    public void startTask(BukkitRunnable task, long delay) {
        task.runTaskTimer(Neptune.get(), delay, 20L);
        tasks.add(task);
    }

    public void startTask(BukkitRunnable task, long delay, long period) {
        task.runTaskTimer(Neptune.get(), delay, period);
        tasks.add(task);
    }


    public void stopAllTasks() {
        for (BukkitRunnable bukkitRunnable : tasks) {
            bukkitRunnable.cancel();
        }
    }
}